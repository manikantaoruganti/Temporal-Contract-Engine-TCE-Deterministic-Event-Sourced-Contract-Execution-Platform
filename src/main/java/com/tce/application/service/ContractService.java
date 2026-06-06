package com.tce.application.service;

import com.tce.api.dto.ContractCreateRequest;
import com.tce.api.dto.ContractResponse;
import com.tce.api.dto.RuleResponse;
import com.tce.domain.event.ContractCancelledEvent;
import com.tce.domain.event.ContractCreatedEvent;
import com.tce.domain.model.Contract;
import com.tce.domain.model.ContractStatus;
import com.tce.domain.model.Rule;
import com.tce.domain.repository.ContractRepository;
import com.tce.domain.repository.RuleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractService {

    private final ContractRepository contractRepository;
    private final RuleRepository ruleRepository;
    private final EventPublisher eventPublisher;
    private final MetricsService metricsService;

    @Transactional
    @CacheEvict(value = "contracts", allEntries = true)
    public ContractResponse createContract(ContractCreateRequest request) {
        log.info("Creating new contract: {}", request.getName());

        List<Rule> rules = request.getRuleIds() != null ?
                ruleRepository.findAllById(request.getRuleIds()) : List.of();

        if (request.getRuleIds() != null && rules.size() != request.getRuleIds().size()) {
            throw new IllegalArgumentException("One or more specified rules not found.");
        }

        Contract contract = Contract.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(ContractStatus.PENDING)
                .effectiveDate(request.getEffectiveDate())
                .expirationDate(request.getExpirationDate())
                .build();

        Contract savedContract = contractRepository.save(contract);
        log.debug("Contract saved with ID: {}", savedContract.getId());

        // Associate rules with the contract using the owning-side helper
        for (Rule rule : rules) {
            savedContract.addRule(rule);
        }
        if (!rules.isEmpty()) {
            contractRepository.save(savedContract);
        }

        // Record metrics
        metricsService.incrementContractCreation();

        eventPublisher.publish(new ContractCreatedEvent(
                savedContract.getId(),
                savedContract.getName(),
                savedContract.getDescription(),
                savedContract.getEffectiveDate(),
                savedContract.getExpirationDate(),
                Instant.now()));
        log.info("ContractCreatedEvent published for contract ID: {}", savedContract.getId());

        return mapToContractResponse(savedContract);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "contracts", key = "#id")
    public ContractResponse getContractById(UUID id) {
        log.debug("Fetching contract by ID: {}", id);
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found with ID: " + id));
        return mapToContractResponse(contract);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "contracts")
    public List<ContractResponse> getAllContracts() {
        log.debug("Fetching all contracts");
        return contractRepository.findAll().stream()
                .map(this::mapToContractResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Contract> getExecutableContractsForExecution() {
        log.debug("Fetching executable contracts for execution check.");
        return contractRepository.findExecutableContracts(Instant.now());
    }

    @Transactional
    @CacheEvict(value = "contracts", key = "#contractId")
    public void updateContractStatus(UUID contractId, ContractStatus newStatus) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found with ID: " + contractId));
        ContractStatus oldStatus = contract.getStatus();

        // Validate state transition using the State Machine
        com.tce.domain.model.ContractStateMachine.validateTransition(oldStatus, newStatus);

        contract.setStatus(newStatus);
        contract.setLastModifiedDate(Instant.now());
        contractRepository.save(contract);
        log.info("Contract ID {} status changed from {} to {}", contractId, oldStatus, newStatus);

        // Emit a domain event for EVERY status transition so replay is always consistent
        if (newStatus == ContractStatus.CANCELLED) {
            eventPublisher.publish(new ContractCancelledEvent(
                    contractId,
                    "Manually cancelled via API (previous status: " + oldStatus + ")",
                    Instant.now()
            ));
        }
        // Additional status transitions that may come from external callers can be added here
    }

    private ContractResponse mapToContractResponse(Contract contract) {
        List<RuleResponse> ruleResponses = contract.getRules().stream()
                .map(rule -> RuleResponse.builder()
                        .id(rule.getId())
                        .name(rule.getName())
                        .description(rule.getDescription())
                        .conditionExpression(rule.getConditionExpression())
                        .actionPayload(rule.getActionPayload())
                        .priority(rule.getPriority())
                        .contractId(contract.getId())
                        .creationDate(rule.getCreationDate())
                        .lastModifiedDate(rule.getLastModifiedDate())
                        .build())
                .collect(Collectors.toList());

        return ContractResponse.builder()
                .id(contract.getId())
                .name(contract.getName())
                .description(contract.getDescription())
                .status(contract.getStatus())
                .effectiveDate(contract.getEffectiveDate())
                .expirationDate(contract.getExpirationDate())
                .creationDate(contract.getCreationDate())
                .lastModifiedDate(contract.getLastModifiedDate())
                .rules(ruleResponses)
                .build();
    }
}
