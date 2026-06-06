package com.tce.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@Slf4j
public class ConflictResolver {

    /**
     * Executes a database-modifying operation. If an optimistic locking conflict occurs,
     * it retries the operation up to maxRetries times with linear backoff.
     */
    public <T> T runWithRetry(Supplier<T> action, int maxRetries, long backoffMs) {
        int attempt = 0;
        while (true) {
            try {
                return action.get();
            } catch (ObjectOptimisticLockingFailureException e) {
                attempt++;
                if (attempt >= maxRetries) {
                    log.error("Optimistic locking conflict: retry limit reached ({})", maxRetries, e);
                    throw e;
                }
                log.warn("Optimistic locking conflict detected. Retrying operation (attempt {}/{}) in {}ms...", 
                        attempt, maxRetries, backoffMs * attempt);
                try {
                    Thread.sleep(backoffMs * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }
    }
}
