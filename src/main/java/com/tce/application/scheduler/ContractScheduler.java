package com.tce.application.scheduler;

import com.tce.application.service.ContractService;
import com.tce.application.service.ContractExecutionService;
import com.tce.application.service.MetricsService;
import com.tce.domain.model.Contract;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContractScheduler {

    private final ContractService contractService;
    private final ContractExecutionService contractExecutionService;
    private final MetricsService metricsService;

    /**
     * Scheduled task to fetch executable contracts and execute their rules.
     * Runs every 30 seconds.
     */
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void processPendingContracts() {
        log.info("Scheduler: Checking for executable contracts to process at {}", Instant.now());

        List<Contract> executableContracts = contractService.getExecutableContractsForExecution();

        // Track backlog in Micrometer gauge
        metricsService.setSchedulerBacklog(executableContracts.size());

        if (executableContracts.isEmpty()) {
            log.debug("Scheduler: No executable contracts found.");
            return;
        }

        log.info("Scheduler: Found {} executable contracts for processing.", executableContracts.size());

        // Define default evaluation facts for automated background execution.
        // In production, these facts would be fetched dynamically from user profiles or transactions.
        Map<String, Object> backgroundFacts = Map.of(
                "balance", 1200.0,
                "risk_score", 25.0
        );

        for (Contract contract : executableContracts) {
            log.info("Scheduler: Executing contract ID: {} (Name: {})", contract.getId(), contract.getName());
            try {
                contractExecutionService.executeContract(contract.getId(), backgroundFacts);
            } catch (Exception e) {
                log.error("Scheduler: Exception executing contract ID {}: {}", contract.getId(), e.getMessage(), e);
            }
        }
        log.info("Scheduler: Finished processing executable contracts.");
    }
}
