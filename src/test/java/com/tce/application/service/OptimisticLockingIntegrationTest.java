package com.tce.application.service;

import com.tce.BaseIntegrationTest;
import com.tce.domain.model.Contract;
import com.tce.domain.model.ContractStatus;
import com.tce.domain.repository.ContractRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class OptimisticLockingIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ConflictResolver conflictResolver;

    @Test
    public void testVersionIncrementOnSave() {
        Contract contract = Contract.builder()
                .name("Optimistic Contract")
                .status(ContractStatus.PENDING)
                .effectiveDate(Instant.now())
                .build();

        Contract saved = contractRepository.saveAndFlush(contract);
        assertEquals(0, saved.getVersion(), "Initial version should be 0");

        saved.setName("Optimistic Contract Updated");
        Contract updated = contractRepository.saveAndFlush(saved);
        assertEquals(1, updated.getVersion(), "Version should increment to 1 on update");
    }

    @Test
    public void testConcurrentModificationFailure() throws InterruptedException, ExecutionException {
        Contract contract = Contract.builder()
                .name("Conflict Contract")
                .status(ContractStatus.PENDING)
                .effectiveDate(Instant.now())
                .build();

        Contract saved = contractRepository.saveAndFlush(contract);
        UUID contractId = saved.getId();

        // Load two separate instances of the same contract (mimicking concurrent transactions)
        Contract instance1 = contractRepository.findById(contractId).orElseThrow();
        Contract instance2 = contractRepository.findById(contractId).orElseThrow();

        // Update instance1 -> increments version in DB to 1
        instance1.setName("Name Updated by T1");
        contractRepository.saveAndFlush(instance1);

        // Update instance2 -> should fail with optimistic locking failure because DB version is 1, but instance2 version is 0
        instance2.setName("Name Updated by T2");
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            contractRepository.saveAndFlush(instance2);
        });
    }

    @Test
    public void testConflictResolverRetriesAndSucceeds() {
        Contract contract = Contract.builder()
                .name("Resolver Contract")
                .status(ContractStatus.PENDING)
                .effectiveDate(Instant.now())
                .build();

        Contract saved = contractRepository.saveAndFlush(contract);
        UUID contractId = saved.getId();

        AtomicInteger attemptCounter = new AtomicInteger(0);

        // ConflictResolver should retry when ObjectOptimisticLockingFailureException is simulated
        String finalResult = conflictResolver.runWithRetry(() -> {
            int attempt = attemptCounter.incrementAndGet();
            if (attempt < 3) {
                // Simulate an optimistic locking failure on the first two attempts
                throw new ObjectOptimisticLockingFailureException(Contract.class, contractId);
            }
            return "SUCCESS";
        }, 5, 50);

        assertEquals("SUCCESS", finalResult);
        assertEquals(3, attemptCounter.get(), "Should succeed on the 3rd attempt after 2 retries");
    }
}
