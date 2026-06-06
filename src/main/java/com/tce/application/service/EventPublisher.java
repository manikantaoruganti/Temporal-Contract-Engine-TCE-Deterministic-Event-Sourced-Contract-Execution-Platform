package com.tce.application.service;

import com.tce.domain.event.DomainEvent;

public interface EventPublisher {
    void publish(DomainEvent event);
}
