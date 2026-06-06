package com.tce.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tce.domain.model.Contract;
import com.tce.domain.model.ContractStatus;
import com.tce.domain.model.EventStore;
import com.tce.domain.model.Rule;
import com.tce.domain.repository.ContractRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplayEngineService {

    private final EventStoreService eventStoreService;
    private final ContractRepository contractRepository;
    private final ObjectMapper objectMapper;

    @Data
    @Builder
    public static class ReplayReport {
        private UUID contractId;
        private RebuiltContractState rebuiltState;
        private List<EventSummary> eventsReplayed;
        private boolean isConsistent;
        private List<String> discrepancies;
    }

    @Data
    @Builder
    public static class RebuiltContractState {
        private String name;
        private ContractStatus status;
        private Instant effectiveDate;
        private Instant expirationDate;
        private int rulesCount;
        private List<String> ruleNames;
        private int retryCount;
        private boolean deadLettered;
    }

    @Data
    @Builder
    public static class EventSummary {
        private UUID eventId;
        private String eventType;
        private Instant occurredAt;
    }

    /**
     * Replays all historical events for the given contract ID, reconstructs the
     * contract's state exclusively from events, and verifies consistency against the DB.
     *
     * Invariant #1: Replay(events) == Current Aggregate State
     */
    @Transactional(readOnly = true)
    public ReplayReport replayContract(UUID contractId) {
        log.info("Starting replay audit for contract: {}", contractId);

        Contract actualContract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found with ID: " + contractId));

        List<EventStore> events = eventStoreService.getEventsForAggregate(contractId);
        List<EventSummary> eventsReplayed = new ArrayList<>();
        List<String> discrepancies = new ArrayList<>();

        // --- Projection state reconstructed purely from the event stream ---
        String rebuiltName = null;
        String rebuiltDescription = null;
        ContractStatus rebuiltStatus = null;
        Instant rebuiltEffectiveDate = null;
        Instant rebuiltExpirationDate = null;
        int rebuiltRetryCount = 0;
        boolean rebuiltDeadLettered = false;
        Map<UUID, String> rebuiltRules = new LinkedHashMap<>();

        for (EventStore record : events) {
            eventsReplayed.add(EventSummary.builder()
                    .eventId(record.getEventId())
                    .eventType(record.getEventType())
                    .occurredAt(record.getOccurredAt())
                    .build());

            try {
                JsonNode payload = objectMapper.readTree(record.getPayload());

                switch (record.getEventType()) {

                    case "CONTRACT_CREATED":
                        rebuiltName = safeText(payload, "name");
                        rebuiltDescription = safeText(payload, "description");
                        rebuiltEffectiveDate = safeInstant(payload, "effectiveDate");
                        rebuiltExpirationDate = safeInstant(payload, "expirationDate");
                        rebuiltStatus = ContractStatus.PENDING;
                        rebuiltRetryCount = 0;
                        rebuiltDeadLettered = false;
                        break;

                    case "RULE_ADDED":
                        UUID ruleId = UUID.fromString(payload.get("ruleId").asText());
                        String ruleName = payload.get("ruleName").asText();
                        rebuiltRules.put(ruleId, ruleName);
                        break;

                    case "CONTRACT_EXECUTING":
                        // Transient state — transition marker. Does not persist as final status.
                        rebuiltStatus = ContractStatus.EXECUTING;
                        break;

                    case "CONTRACT_EXECUTED":
                        rebuiltStatus = ContractStatus.COMPLETED;
                        break;

                    case "CONTRACT_CANCELLED":
                        rebuiltStatus = ContractStatus.CANCELLED;
                        break;

                    case "EXECUTION_FAILED":
                        // Each EXECUTION_FAILED represents one retry attempt
                        rebuiltStatus = ContractStatus.FAILED;
                        rebuiltRetryCount++;
                        break;

                    case "CONTRACT_DEAD_LETTERED":
                        rebuiltDeadLettered = true;
                        // Status remains FAILED — DLQ routing doesn't change the contract status
                        break;

                    case "ExecutionLogged":
                    case "EXECUTION_LOGGED":
                        // Audit log marker — does not mutate aggregate state
                        break;

                    default:
                        log.warn("Unhandled event type '{}' during replay for contract {}. Add a handler.",
                                record.getEventType(), contractId);
                        discrepancies.add("Unknown event type encountered during replay: " + record.getEventType());
                }
            } catch (Exception e) {
                log.error("Failed to apply event {} during replay for contract {}", record.getEventId(), contractId, e);
                discrepancies.add("Failed to process event " + record.getEventId() + " (" + record.getEventType() + "): " + e.getMessage());
            }
        }

        // Default if no events have established a status (empty stream edge case)
        if (rebuiltStatus == null) {
            rebuiltStatus = ContractStatus.PENDING;
        }

        // --- Consistency checks: Replay projection vs. live DB state ---

        // Status must match exactly
        if (!rebuiltStatus.equals(actualContract.getStatus())) {
            discrepancies.add(String.format(
                    "Status discrepancy: Replayed=%s, DB=%s", rebuiltStatus, actualContract.getStatus()));
        }

        // Name must match
        if (rebuiltName != null && !rebuiltName.equals(actualContract.getName())) {
            discrepancies.add(String.format(
                    "Name discrepancy: Replayed='%s', DB='%s'", rebuiltName, actualContract.getName()));
        }

        // EffectiveDate must match
        if (rebuiltEffectiveDate != null && !rebuiltEffectiveDate.equals(actualContract.getEffectiveDate())) {
            discrepancies.add(String.format(
                    "EffectiveDate discrepancy: Replayed=%s, DB=%s", rebuiltEffectiveDate, actualContract.getEffectiveDate()));
        }

        // Rule count must match
        if (rebuiltRules.size() != actualContract.getRules().size()) {
            discrepancies.add(String.format(
                    "Rules count discrepancy: Replayed=%d, DB=%d", rebuiltRules.size(), actualContract.getRules().size()));
        } else {
            for (Rule rule : actualContract.getRules()) {
                if (!rebuiltRules.containsKey(rule.getId())) {
                    discrepancies.add(String.format(
                            "Rule ID %s ('%s') present in DB but missing from replayed event stream.",
                            rule.getId(), rule.getName()));
                }
            }
        }

        // Retry count must match
        if (rebuiltRetryCount != actualContract.getRetryCount()) {
            discrepancies.add(String.format(
                    "Retry count discrepancy: Replayed=%d, DB=%d", rebuiltRetryCount, actualContract.getRetryCount()));
        }

        RebuiltContractState rebuiltState = RebuiltContractState.builder()
                .name(rebuiltName)
                .status(rebuiltStatus)
                .effectiveDate(rebuiltEffectiveDate)
                .expirationDate(rebuiltExpirationDate)
                .rulesCount(rebuiltRules.size())
                .ruleNames(new ArrayList<>(rebuiltRules.values()))
                .retryCount(rebuiltRetryCount)
                .deadLettered(rebuiltDeadLettered)
                .build();

        boolean isConsistent = discrepancies.isEmpty();
        log.info("Replay audit finished for contract {}. Consistent: {}", contractId, isConsistent);

        return ReplayReport.builder()
                .contractId(contractId)
                .rebuiltState(rebuiltState)
                .eventsReplayed(eventsReplayed)
                .isConsistent(isConsistent)
                .discrepancies(discrepancies)
                .build();
    }

    private String safeText(JsonNode node, String field) {
        return (node.has(field) && !node.get(field).isNull()) ? node.get(field).asText() : null;
    }

    private Instant safeInstant(JsonNode node, String field) {
        String val = safeText(node, field);
        return val != null ? Instant.parse(val) : null;
    }
}
