package com.tce.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@ToString
public class ContractExecutingEvent implements DomainEvent {
    private final UUID eventId = UUID.randomUUID();
    private final UUID contractId;
    private final Instant timestamp;

    @Override
    public String getEventType() {
        return "CONTRACT_EXECUTING";
    }
}
