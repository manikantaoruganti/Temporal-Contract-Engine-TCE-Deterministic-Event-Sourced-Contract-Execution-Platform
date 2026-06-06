package com.tce.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "execution_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID contractId; // Reference to the contract that was executed

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant executionTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status; // ATTEMPTED, SUCCESS, FAILED

    @Column(columnDefinition = "TEXT")
    private String details; // JSON or plain text details about the execution result
}
