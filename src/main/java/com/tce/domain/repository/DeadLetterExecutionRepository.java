package com.tce.domain.repository;

import com.tce.domain.model.DeadLetterExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeadLetterExecutionRepository extends JpaRepository<DeadLetterExecution, UUID> {
}
