package com.tce.application.service;

import com.tce.domain.event.ContractDeadLetteredEvent;
import com.tce.domain.event.ContractExecutedEvent;
import com.tce.domain.event.ContractExecutingEvent;
import com.tce.domain.event.ExecutionFailedEvent;
import com.tce.domain.model.*;
import com.tce.domain.repository.ContractRepository;
import com.tce.domain.repository.DeadLetterExecutionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractExecutionService {

    private final ContractRepository contractRepository;
    private final DeadLetterExecutionRepository deadLetterExecutionRepository;
    private final RuleEvaluator ruleEvaluator;
    private final ExecutionLogService executionLogService;
    private final EventPublisher eventPublisher;
    private final MetricsService metricsService;
    private final ConflictResolver conflictResolver;

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY_SEC = 5;

    /**
     * Executes a contract by evaluating its associated rules against facts.
     * Retries automatically if optimistic locking conflicts occur.
     */
    public void executeContract(UUID contractId, Map<String, Object> facts) {
        long startTime = System.currentTimeMillis();
        log.info("Initiating execution flow for contract ID: {}", contractId);

        if (facts == null) {
            facts = new HashMap<>();
        }

        facts.putIfAbsent("attempt_time", Instant.now().toString());

        Map<String, Object> finalFacts = facts;

        conflictResolver.runWithRetry(() -> {
            executeInsideTransaction(contractId, finalFacts, startTime);
            return null;
        }, 3, 100);
    }

    @Transactional
    public void executeInsideTransaction(UUID contractId, Map<String, Object> facts, long startTime) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found with ID: " + contractId));

        ContractStatus currentStatus = contract.getStatus();

        // 1. Validate State Transition to EXECUTING
        ContractStateMachine.validateTransition(currentStatus, ContractStatus.EXECUTING);

        // Apply transition + emit event — every DB mutation must have a corresponding event
        contract.setStatus(ContractStatus.EXECUTING);
        contract.setLastModifiedDate(Instant.now());
        contract = contractRepository.saveAndFlush(contract);

        // Invariant #2: emit CONTRACT_EXECUTING event immediately after persisting the state change
        eventPublisher.publish(new ContractExecutingEvent(contractId, Instant.now()));

        // 2. Evaluate Rules
        try {
            boolean allRulesPassed = true;
            StringBuilder executionDetails = new StringBuilder("Rule Evaluation Results:\n");

            long ruleStart = System.currentTimeMillis();
            for (Rule rule : contract.getRules()) {
                facts.put("attempt_count", contract.getRetryCount() + 1);

                boolean passed = ruleEvaluator.evaluate(rule.getConditionExpression(), facts);
                executionDetails.append(String.format("- Rule '%s' (priority %d): %s\n",
                        rule.getName(), rule.getPriority(), passed ? "PASSED" : "FAILED"));

                if (!passed) {
                    allRulesPassed = false;
                }
            }
            metricsService.recordRuleEvaluationLatency(System.currentTimeMillis() - ruleStart);

            if (contract.getRules().isEmpty()) {
                executionDetails.append("No rules associated with this contract. Evaluated as PASSED.\n");
            }

            if (allRulesPassed) {
                // 3. Successful execution path
                contract.setStatus(ContractStatus.COMPLETED);
                contract.setNextAttemptAt(null);
                contractRepository.save(contract);

                executionLogService.createExecutionLog(
                        contractId,
                        ExecutionStatus.SUCCESS,
                        executionDetails.toString() + "\nContract fully executed successfully."
                );

                // Invariant #2: CONTRACT_EXECUTED event emitted immediately after DB mutation
                eventPublisher.publish(new ContractExecutedEvent(contractId, "Successful execution", Instant.now()));
                metricsService.incrementExecutionSuccess();
                metricsService.recordExecutionLatency(System.currentTimeMillis() - startTime);

                log.info("Contract ID {} successfully executed.", contractId);
            } else {
                throw new RuntimeException("Rule evaluation conditions not fully met.\n" + executionDetails);
            }
        } catch (Exception e) {
            handleExecutionFailure(contract, e, "", startTime);
        }
    }

    private void handleExecutionFailure(Contract contract, Exception exception, String currentDetails, long startTime) {
        UUID contractId = contract.getId();
        int retryAttempt = contract.getRetryCount();

        metricsService.incrementExecutionFailure();
        metricsService.recordExecutionLatency(System.currentTimeMillis() - startTime);

        if (retryAttempt < MAX_RETRIES) {
            // Exponential backoff retry
            long delaySec = INITIAL_RETRY_DELAY_SEC * (long) Math.pow(2, retryAttempt);
            contract.setStatus(ContractStatus.FAILED);
            contract.setRetryCount(retryAttempt + 1);
            contract.setNextAttemptAt(Instant.now().plusSeconds(delaySec));
            contract.setLastModifiedDate(Instant.now());
            contractRepository.save(contract);

            String failureDetails = String.format("%s\nExecution attempt %d failed: %s. Retrying in %d seconds at %s.",
                    currentDetails, retryAttempt + 1, exception.getMessage(), delaySec, contract.getNextAttemptAt());

            executionLogService.createExecutionLog(
                    contractId,
                    ExecutionStatus.FAILED,
                    failureDetails
            );

            // Invariant #2: EXECUTION_FAILED event emitted after DB mutation
            eventPublisher.publish(new ExecutionFailedEvent(contractId, exception.getMessage(), Instant.now()));
            log.warn("Contract execution attempt {} failed for ID: {}. Next retry scheduled at {}",
                    retryAttempt + 1, contractId, contract.getNextAttemptAt());
        } else {
            // Permanent failure — route to Dead Letter Queue
            contract.setStatus(ContractStatus.FAILED);
            contract.setNextAttemptAt(null);
            contract.setLastModifiedDate(Instant.now());
            contractRepository.save(contract);

            String stackTrace = getStackTraceAsString(exception);
            DeadLetterExecution dlq = DeadLetterExecution.builder()
                    .contractId(contractId)
                    .failedAt(Instant.now())
                    .errorMessage(exception.getMessage())
                    .stackTrace(stackTrace)
                    .build();

            DeadLetterExecution savedDlq = deadLetterExecutionRepository.save(dlq);

            String failureDetails = String.format("%s\nMax retries (%d) exhausted. Contract permanently failed. Routed to Dead Letter Table.",
                    currentDetails, MAX_RETRIES);

            executionLogService.createExecutionLog(
                    contractId,
                    ExecutionStatus.FAILED,
                    failureDetails
            );

            // Invariant #2: Emit both EXECUTION_FAILED and CONTRACT_DEAD_LETTERED events for complete replay trail
            eventPublisher.publish(new ExecutionFailedEvent(contractId, "Max retries exhausted: " + exception.getMessage(), Instant.now()));
            eventPublisher.publish(new ContractDeadLetteredEvent(
                    contractId,
                    savedDlq.getId(),
                    exception.getMessage(),
                    MAX_RETRIES,
                    Instant.now()
            ));
            log.error("Contract ID {} failed permanently after {} retries. Routed to DLQ.", contractId, MAX_RETRIES);
        }
    }

    private String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
