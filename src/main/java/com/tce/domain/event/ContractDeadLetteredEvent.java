package com.tce.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@ToString
public class ContractDeadLetteredEvent implements DomainEvent {
    private final UUID eventId = UUID.randomUUID();
    private final UUID contractId;
    private final UUID deadLetterRecordId;
    private final String errorMessage;
    private final int totalRetriesAttempted;
    private final Instant timestamp;

    @Override
    public String getEventType() {
        return "CONTRACT_DEAD_LETTERED";
    }
}
