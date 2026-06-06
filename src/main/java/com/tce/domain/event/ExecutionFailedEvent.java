package com.tce.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@ToString
public class ExecutionFailedEvent implements DomainEvent {
    private final UUID eventId = UUID.randomUUID();
    private final UUID contractId;
    private final String errorMessage;
    private final Instant timestamp;

    @Override
    public String getEventType() {
        return "EXECUTION_FAILED";
    }
}
