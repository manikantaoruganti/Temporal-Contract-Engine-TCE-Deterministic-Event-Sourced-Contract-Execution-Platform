package com.tce.domain.event;

import com.tce.domain.model.ExecutionStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@ToString
public class ExecutionLoggedEvent implements DomainEvent {
    private final UUID eventId = UUID.randomUUID();
    private final UUID logId;
    private final UUID contractId;
    private final ExecutionStatus status;
    private final Instant timestamp;

    @Override
    public String getEventType() {
        return "ExecutionLogged";
    }
}
