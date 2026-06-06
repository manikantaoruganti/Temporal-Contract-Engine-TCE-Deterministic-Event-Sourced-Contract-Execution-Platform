package com.tce.application.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MetricsService {

    private final Counter contractCreationCounter;
    private final Counter executionSuccessCounter;
    private final Counter executionFailureCounter;
    private final Timer ruleEvaluationTimer;
    private final Timer executionTimer;
    private final AtomicInteger schedulerBacklogGauge = new AtomicInteger(0);

    public MetricsService(MeterRegistry meterRegistry) {
        this.contractCreationCounter = Counter.builder("contract_creation_count")
                .description("Total number of contracts created")
                .register(meterRegistry);

        this.executionSuccessCounter = Counter.builder("execution_success_count")
                .description("Total number of successfully executed contracts")
                .register(meterRegistry);

        this.executionFailureCounter = Counter.builder("execution_failure_count")
                .description("Total number of failed contract execution attempts")
                .register(meterRegistry);

        this.ruleEvaluationTimer = Timer.builder("rule_evaluation_latency")
                .description("Time taken to evaluate rules")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(meterRegistry);

        this.executionTimer = Timer.builder("execution_latency")
                .description("Total time taken for contract execution flow")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(meterRegistry);

        meterRegistry.gauge("scheduler_backlog", schedulerBacklogGauge);
    }

    public void incrementContractCreation() {
        contractCreationCounter.increment();
    }

    public void incrementExecutionSuccess() {
        executionSuccessCounter.increment();
    }

    public void incrementExecutionFailure() {
        executionFailureCounter.increment();
    }

    public void recordRuleEvaluationLatency(long durationMs) {
        ruleEvaluationTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordExecutionLatency(long durationMs) {
        executionTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void setSchedulerBacklog(int backlog) {
        schedulerBacklogGauge.set(backlog);
    }
}
