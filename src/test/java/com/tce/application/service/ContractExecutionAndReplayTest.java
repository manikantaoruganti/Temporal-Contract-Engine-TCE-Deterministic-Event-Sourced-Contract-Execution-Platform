package com.tce.application.service;

import com.tce.BaseIntegrationTest;
import com.tce.api.dto.ContractCreateRequest;
import com.tce.api.dto.ContractResponse;
import com.tce.api.dto.RuleCreateRequest;
import com.tce.domain.model.Contract;
import com.tce.domain.model.ContractStatus;
import com.tce.domain.model.EventStore;
import com.tce.domain.repository.ContractRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ContractExecutionAndReplayTest extends BaseIntegrationTest {

    @Autowired
    private ContractService contractService;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private ContractExecutionService contractExecutionService;

    @Autowired
    private ReplayEngineService replayEngineService;

    @Autowired
    private EventStoreService eventStoreService;

    @Autowired
    private ContractRepository contractRepository;

    @Test
    public void testExecutionAndStateReplay() {
        // 1. Create a new contract
        ContractCreateRequest contractRequest = new ContractCreateRequest();
        contractRequest.setName("Replay Testing Contract");
        contractRequest.setDescription("Verifying event sourced state rebuilding");
        contractRequest.setEffectiveDate(Instant.now());

        ContractResponse contractResponse = contractService.createContract(contractRequest);
        UUID contractId = contractResponse.getId();
        assertNotNull(contractId);

        // 2. Add Rules to the contract (evaluates to true with default context: balance = 1200, risk_score = 25)
        RuleCreateRequest ruleRequest1 = new RuleCreateRequest();
        ruleRequest1.setContractId(contractId);
        ruleRequest1.setName("Rule 1: Balance constraint");
        ruleRequest1.setConditionExpression("balance > 1000");
        ruleRequest1.setActionPayload("{\"action\":\"DISBURSE_FUNDS\"}");
        ruleRequest1.setPriority(1);
        ruleService.createRule(ruleRequest1);

        RuleCreateRequest ruleRequest2 = new RuleCreateRequest();
        ruleRequest2.setContractId(contractId);
        ruleRequest2.setName("Rule 2: Risk constraint");
        ruleRequest2.setConditionExpression("risk_score < 30");
        ruleRequest2.setActionPayload("{\"action\":\"LOG_RISK\"}");
        ruleRequest2.setPriority(2);
        ruleService.createRule(ruleRequest2);

        // Verify rules are linked
        Contract activeContract = contractRepository.findById(contractId).orElseThrow();
        assertEquals(2, activeContract.getRules().size());

        // Verify events persisted in EventStore: CONTRACT_CREATED and 2 RULE_ADDED events
        List<EventStore> eventsBeforeExec = eventStoreService.getEventsForAggregate(contractId);
        assertTrue(eventsBeforeExec.stream().anyMatch(e -> "CONTRACT_CREATED".equals(e.getEventType())));
        assertEquals(2, eventsBeforeExec.stream().filter(e -> "RULE_ADDED".equals(e.getEventType())).count());

        // 3. Execute Contract with positive facts -> should pass and set status to COMPLETED
        Map<String, Object> facts = Map.of(
                "balance", 1500.0,
                "risk_score", 20.0
        );
        contractExecutionService.executeContract(contractId, facts);

        // Verify DB status updated to COMPLETED
        Contract postExecContract = contractRepository.findById(contractId).orElseThrow();
        assertEquals(ContractStatus.COMPLETED, postExecContract.getStatus());

        // Verify CONTRACT_EXECUTED event is logged
        List<EventStore> eventsAfterExec = eventStoreService.getEventsForAggregate(contractId);
        assertTrue(eventsAfterExec.stream().anyMatch(e -> "CONTRACT_EXECUTED".equals(e.getEventType())));

        // 4. Run Replay Engine and verify consistency report
        ReplayEngineService.ReplayReport report = replayEngineService.replayContract(contractId);
        assertNotNull(report);
        assertEquals(contractId, report.getContractId());
        assertTrue(report.isConsistent(), "Rebuilt state should be fully consistent with DB: " + report.getDiscrepancies());
        assertEquals(ContractStatus.COMPLETED, report.getRebuiltState().getStatus());
        assertEquals(2, report.getRebuiltState().getRulesCount());
        assertTrue(report.getDiscrepancies().isEmpty());
    }
}
