package com.tce.domain.event;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID getEventId();
    Instant getTimestamp();
    String getEventType();
}
