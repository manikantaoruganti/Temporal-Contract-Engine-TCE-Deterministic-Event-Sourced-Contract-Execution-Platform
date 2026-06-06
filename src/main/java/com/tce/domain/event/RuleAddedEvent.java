package com.tce.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@ToString
public class RuleAddedEvent implements DomainEvent {
    private final UUID eventId = UUID.randomUUID();
    private final UUID contractId;
    private final UUID ruleId;
    private final String ruleName;
    private final String conditionExpression;
    private final String actionPayload;
    private final int priority;
    private final Instant timestamp;

    @Override
    public String getEventType() {
        return "RULE_ADDED";
    }
}
