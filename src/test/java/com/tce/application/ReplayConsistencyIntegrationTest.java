package com.tce.application;

import com.tce.BaseIntegrationTest;
import com.tce.api.dto.ContractCreateRequest;
import com.tce.api.dto.ContractResponse;
import com.tce.api.dto.RuleCreateRequest;
import com.tce.application.service.ContractExecutionService;
import com.tce.application.service.ContractService;
import com.tce.application.service.ReplayEngineService;
import com.tce.application.service.RuleService;
import com.tce.domain.model.ContractStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests proving Replay(events) == Database State for all 7 lifecycle workflows.
 *
 * Each test exercises one workflow, then calls replayContract() and asserts:
 *   - isConsistent == true
 *   - discrepancies is empty
 */
@DisplayName("Replay Consistency Integration Tests")
class ReplayConsistencyIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ContractService contractService;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private ContractExecutionService contractExecutionService;

    @Autowired
    private ReplayEngineService replayEngineService;

    // ───────────────────────────────────────────────────────────────────────────
    // Workflow 1: Create Contract → Replay  (consistent=true)
    // ───────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Workflow 1: Create Contract → Replay returns consistent=true")
    void workflow1_createContract_replay_isConsistent() {
        ContractResponse contract = contractService.createContract(
                ContractCreateRequest.builder()
                        .name("W1 Contract")
                        .description("Workflow 1")
                        .effectiveDate(Instant.now())
                        .build()
        );

        ReplayEngineService.ReplayReport report = replayEngineService.replayContract(contract.getId());

        assertThat(report.isConsistent())
                .as("Workflow 1: Replay discrepancies: " + report.getDiscrepancies())
                .isTrue();
        assertThat(report.getDiscrepancies()).isEmpty();
        assertThat(report.getRebuiltState().getStatus()).isEqualTo(ContractStatus.PENDING);
    }

    // ───────────────────────────────────────────────────────────────────────────
    // Workflow 2: Create Contract → Add Rule → Replay  (consistent=true)
    // ───────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Workflow 2: Create Contract + Add Rule → Replay returns consistent=true")
    void workflow2_createContractWithRule_replay_isConsistent() {
        ContractResponse contract = contractService.createContract(
                ContractCreateRequest.builder()
                        .name("W2 Contract")
                        .effectiveDate(Instant.now())
                        .build()
        );

        ruleService.createRule(
                RuleCreateRequest.builder()
                        .contractId(contract.getId())
                        .name("Balance Rule")
                        .conditionExpression("balance > 1000")
                        .actionPayload("{\"action\":\"approve\"}")
                        .priority(1)
                        .build()
        );

        ReplayEngineService.ReplayReport report = replayEngineService.replayContract(contract.getId());

        assertThat(report.isConsistent())
                .as("Workflow 2: Replay discrepancies: " + report.getDiscrepancies())
                .isTrue();
        assertThat(report.getDiscrepancies()).isEmpty();
        assertThat(report.getRebuiltState().getRulesCount()).isEqualTo(1);
    }

    // ───────────────────────────────────────────────────────────────────────────
    // Workflow 3: Create Contract → Execute Success → Replay  (consistent=true)
    // ───────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Workflow 3: Create + Execute Success → Replay returns consistent=true")
    void workflow3_executeSuccess_replay_isConsistent() {
        ContractResponse contract = contractService.createContract(
                ContractCreateRequest.builder()
                        .name("W3 Contract")
                        .effectiveDate(Instant.now())
                        .build()
        );

        // No rules => auto-passes
        contractExecutionService.executeContract(contract.getId(), Map.of("balance", 9999.0));

        ReplayEngineService.ReplayReport report = replayEngineService.replayContract(contract.getId());

        assertThat(report.isConsistent())
                .as("Workflow 3: Replay discrepancies: " + report.getDiscrepancies())
                .isTrue();
        assertThat(report.getRebuiltState().getStatus()).isEqualTo(ContractStatus.COMPLETED);
    }

    // ───────────────────────────────────────────────────────────────────────────
    // Workflow 4: Create Contract → Execute Failure → Replay  (consistent=true)
    // ───────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Workflow 4: Create + Execute Failure → Replay returns consistent=true")
    void workflow4_executeFailure_replay_isConsistent() {
        ContractResponse contract = contractService.createContract(
                ContractCreateRequest.builder()
                        .name("W4 Contract")
                        .effectiveDate(Instant.now())
                        .build()
        );

        ruleService.createRule(
                RuleCreateRequest.builder()
                        .contractId(contract.getId())
                        .name("Fail Rule")
                        .conditionExpression("balance > 99999")
                        .actionPayload("{}")
                        .priority(1)
                        .build()
        );

        try {
            contractExecutionService.executeContract(contract.getId(), Map.of("balance", 1.0));
        } catch (Exception ignored) {
            // Expected: rule not met
        }

        ReplayEngineService.ReplayReport report = replayEngineService.replayContract(contract.getId());

        assertThat(report.isConsistent())
                .as("Workflow 4: Replay discrepancies: " + report.getDiscrepancies())
                .isTrue();
        assertThat(report.getRebuiltState().getStatus()).isEqualTo(ContractStatus.FAILED);
    }

    // ───────────────────────────────────────────────────────────────────────────
    // Workflow 5: Create Contract → Execute Failure → Cancel → Replay (consistent=true)
    // ───────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Workflow 5: Create + Fail + Cancel → Replay returns consistent=true")
    void workflow5_failThenCancel_replay_isConsistent() {
        ContractResponse contract = contractService.createContract(
                ContractCreateRequest.builder()
                        .name("W5 Contract")
                        .effectiveDate(Instant.now())
                        .build()
        );

        ruleService.createRule(
                RuleCreateRequest.builder()
                        .contractId(contract.getId())
                        .name("Always Fail")
                        .conditionExpression("balance > 99999")
                        .actionPayload("{}")
                        .priority(1)
                        .build()
        );

        try {
            contractExecutionService.executeContract(contract.getId(), Map.of("balance", 1.0));
        } catch (Exception ignored) { }

        // Now cancel the failed contract
        contractService.updateContractStatus(contract.getId(), ContractStatus.CANCELLED);

        ReplayEngineService.ReplayReport report = replayEngineService.replayContract(contract.getId());

        assertThat(report.isConsistent())
                .as("Workflow 5: Replay discrepancies: " + report.getDiscrepancies())
                .isTrue();
        assertThat(report.getRebuiltState().getStatus()).isEqualTo(ContractStatus.CANCELLED);
    }

    // ───────────────────────────────────────────────────────────────────────────
    // Workflow 6: Create Contract → Multiple Retries → Replay  (consistent=true)
    // ───────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Workflow 6: Create + Multiple Retries → Replay returns consistent=true")
    void workflow6_multipleRetries_replay_isConsistent() {
        ContractResponse contract = contractService.createContract(
                ContractCreateRequest.builder()
                        .name("W6 Contract")
                        .effectiveDate(Instant.now())
                        .build()
        );

        ruleService.createRule(
                RuleCreateRequest.builder()
                        .contractId(contract.getId())
                        .name("Impossible Rule")
                        .conditionExpression("balance > 99999")
                        .actionPayload("{}")
                        .priority(1)
                        .build()
        );

        // Execute twice — each will fail and increment retry count
        for (int i = 0; i < 2; i++) {
            try {
                contractExecutionService.executeContract(contract.getId(), Map.of("balance", 1.0));
            } catch (Exception ignored) { }
        }

        ReplayEngineService.ReplayReport report = replayEngineService.replayContract(contract.getId());

        assertThat(report.isConsistent())
                .as("Workflow 6: Replay discrepancies: " + report.getDiscrepancies())
                .isTrue();
        assertThat(report.getRebuiltState().getRetryCount()).isEqualTo(2);
    }

    // ───────────────────────────────────────────────────────────────────────────
    // Workflow 7: Create → Exhaust Retries → Dead Letter → Replay (consistent=true)
    // ───────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Workflow 7: Create + Exhaust Retries (Dead Letter) → Replay returns consistent=true")
    void workflow7_deadLetter_replay_isConsistent() {
        ContractResponse contract = contractService.createContract(
                ContractCreateRequest.builder()
                        .name("W7 Contract")
                        .effectiveDate(Instant.now())
                        .build()
        );

        ruleService.createRule(
                RuleCreateRequest.builder()
                        .contractId(contract.getId())
                        .name("DLQ Trigger Rule")
                        .conditionExpression("balance > 99999")
                        .actionPayload("{}")
                        .priority(1)
                        .build()
        );

        // Execute MAX_RETRIES + 1 times to exhaust retries and trigger DLQ routing
        for (int i = 0; i <= 3; i++) {
            try {
                contractExecutionService.executeContract(contract.getId(), Map.of("balance", 1.0));
            } catch (Exception ignored) { }
        }

        ReplayEngineService.ReplayReport report = replayEngineService.replayContract(contract.getId());

        assertThat(report.isConsistent())
                .as("Workflow 7: Replay discrepancies: " + report.getDiscrepancies())
                .isTrue();
        assertThat(report.getRebuiltState().isDeadLettered()).isTrue();
        assertThat(report.getRebuiltState().getStatus()).isEqualTo(ContractStatus.FAILED);
    }
}
