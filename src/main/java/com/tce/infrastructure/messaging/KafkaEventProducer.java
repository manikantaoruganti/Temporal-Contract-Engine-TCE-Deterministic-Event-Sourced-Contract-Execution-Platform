package com.tce.infrastructure.messaging;

import com.tce.application.service.EventPublisher;
import com.tce.application.service.EventStoreService;
import com.tce.config.KafkaConfig;
import com.tce.domain.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducer implements EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final EventStoreService eventStoreService;

    @Override
    public void publish(DomainEvent event) {
        // Persist to Event Store first — transactional guarantee via REQUIRED propagation
        try {
            eventStoreService.store(event);
        } catch (Exception e) {
            log.error("Failed to persist event '{}' to EventStore: {}", event.getEventType(), e.getMessage(), e);
            // Re-throw so the calling transaction is aware of the failure
            throw new RuntimeException("EventStore persistence failed for event: " + event.getEventType(), e);
        }

        String topic = resolveTopic(event);
        String key = resolveKey(event);

        log.debug("Publishing event '{}' to topic: {}, key: {}", event.getEventType(), topic, key);
        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.debug("Event '{}' published successfully to topic {} at offset {}",
                                event.getEventType(), topic, result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to publish event '{}' to topic {}: {}", event.getEventType(), topic, ex.getMessage(), ex);
                    }
                });
    }

    private String resolveTopic(DomainEvent event) {
        if (event instanceof ContractCreatedEvent)      return KafkaConfig.CONTRACT_EVENTS_TOPIC;
        if (event instanceof ContractExecutingEvent)    return KafkaConfig.CONTRACT_EVENTS_TOPIC;
        if (event instanceof ContractExecutedEvent)     return KafkaConfig.CONTRACT_EVENTS_TOPIC;
        if (event instanceof ContractCancelledEvent)    return KafkaConfig.CONTRACT_EVENTS_TOPIC;
        if (event instanceof ContractDeadLetteredEvent) return KafkaConfig.EXECUTION_EVENTS_TOPIC;
        if (event instanceof RuleAddedEvent)            return KafkaConfig.RULE_EVENTS_TOPIC;
        if (event instanceof ExecutionFailedEvent)      return KafkaConfig.EXECUTION_EVENTS_TOPIC;
        if (event instanceof ExecutionLoggedEvent)      return KafkaConfig.EXECUTION_EVENTS_TOPIC;
        log.warn("No Kafka topic mapping for event type: {}. Using default.", event.getEventType());
        return "default-events";
    }

    private String resolveKey(DomainEvent event) {
        if (event instanceof ContractCreatedEvent e)       return e.getContractId().toString();
        if (event instanceof ContractExecutingEvent e)     return e.getContractId().toString();
        if (event instanceof ContractExecutedEvent e)      return e.getContractId().toString();
        if (event instanceof ContractCancelledEvent e)     return e.getContractId().toString();
        if (event instanceof ContractDeadLetteredEvent e)  return e.getContractId().toString();
        if (event instanceof RuleAddedEvent e)             return e.getContractId().toString();
        if (event instanceof ExecutionFailedEvent e)       return e.getContractId().toString();
        if (event instanceof ExecutionLoggedEvent e)       return e.getContractId().toString();
        return event.getEventId().toString();
    }
}
