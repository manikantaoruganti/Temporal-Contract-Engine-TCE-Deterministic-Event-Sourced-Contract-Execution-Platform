package com.tce.domain.repository;

import com.tce.domain.model.EventStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventStoreRepository extends JpaRepository<EventStore, UUID> {
    List<EventStore> findByAggregateIdOrderByOccurredAtAsc(UUID aggregateId);
}
