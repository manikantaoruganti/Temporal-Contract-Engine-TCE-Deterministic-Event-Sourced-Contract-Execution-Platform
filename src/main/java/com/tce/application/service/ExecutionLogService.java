package com.tce.application.service;

import com.tce.domain.event.ExecutionLoggedEvent;
import com.tce.domain.model.ExecutionLog;
import com.tce.domain.model.ExecutionStatus;
import com.tce.domain.repository.ExecutionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutionLogService {

    private final ExecutionLogRepository executionLogRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public ExecutionLog createExecutionLog(UUID contractId, ExecutionStatus status, String details) {
        log.debug("Creating execution log for contract ID: {} with status: {}", contractId, status);
        ExecutionLog logEntry = ExecutionLog.builder()
                .contractId(contractId)
                .status(status)
                .details(details)
                .build();

        ExecutionLog savedLog = executionLogRepository.save(logEntry);
        log.debug("Execution log saved with ID: {}", savedLog.getId());

        eventPublisher.publish(new ExecutionLoggedEvent(savedLog.getId(), contractId, status, Instant.now()));
        log.info("ExecutionLoggedEvent published for log ID: {}", savedLog.getId());

        return savedLog;
    }

    @Transactional(readOnly = true)
    public java.util.List<ExecutionLog> getExecutionLogs(UUID contractId) {
        return executionLogRepository.findByContractIdOrderByExecutionTimestampDesc(contractId);
    }
}
