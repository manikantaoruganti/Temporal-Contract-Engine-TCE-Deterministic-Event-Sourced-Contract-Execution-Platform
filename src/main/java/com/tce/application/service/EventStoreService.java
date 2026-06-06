package com.tce.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tce.domain.event.*;
import com.tce.domain.model.EventStore;
import com.tce.domain.repository.EventStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventStoreService {

    private final EventStoreRepository eventStoreRepository;
    private final ObjectMapper objectMapper;

    /**
     * Appends a domain event to the persistent event store.
     * Participates in the caller's transaction to guarantee atomicity.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void store(DomainEvent event) {
        UUID aggregateId = getAggregateId(event);
        String eventType = event.getEventType();
        String payload;

        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event payload for eventId: {}", event.getEventId(), e);
            throw new RuntimeException("Event serialization failure", e);
        }

        EventStore record = EventStore.builder()
                .eventId(event.getEventId())
                .aggregateId(aggregateId)
                .aggregateType("Contract")
                .eventType(eventType)
                .payload(payload)
                .occurredAt(event.getTimestamp() != null ? event.getTimestamp() : Instant.now())
                .build();

        eventStoreRepository.save(record);
        log.info("Persisted event '{}' for aggregate: {} in EventStore", eventType, aggregateId);
    }

    @Transactional(readOnly = true)
    public List<EventStore> getEventsForAggregate(UUID aggregateId) {
        return eventStoreRepository.findByAggregateIdOrderByOccurredAtAsc(aggregateId);
    }

    /**
     * Resolves the aggregate (Contract) ID from any DomainEvent type.
     * Must be updated whenever a new event type is added.
     */
    private UUID getAggregateId(DomainEvent event) {
        if (event instanceof ContractCreatedEvent e)       return e.getContractId();
        if (event instanceof ContractExecutingEvent e)     return e.getContractId();
        if (event instanceof ContractExecutedEvent e)      return e.getContractId();
        if (event instanceof ContractCancelledEvent e)     return e.getContractId();
        if (event instanceof ContractDeadLetteredEvent e)  return e.getContractId();
        if (event instanceof RuleAddedEvent e)             return e.getContractId();
        if (event instanceof ExecutionFailedEvent e)       return e.getContractId();
        if (event instanceof ExecutionLoggedEvent e)       return e.getContractId();
        throw new IllegalArgumentException("Unknown aggregate mapping for event type: " + event.getEventType()
                + " (" + event.getClass().getName() + ")");
    }
}
