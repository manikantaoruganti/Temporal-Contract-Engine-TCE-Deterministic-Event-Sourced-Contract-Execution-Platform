package com.tce.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "event_store", indexes = {
        @Index(name = "idx_aggregate_id_occurred", columnList = "aggregateId, occurredAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventStore {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID eventId;

    @Column(nullable = false, updatable = false)
    private UUID aggregateId;

    @Column(nullable = false, updatable = false)
    private String aggregateType;

    @Column(nullable = false, updatable = false)
    private String eventType;

    @Column(nullable = false, updatable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false, updatable = false)
    private Instant occurredAt;
}
