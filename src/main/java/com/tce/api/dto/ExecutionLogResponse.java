package com.tce.api.dto;

import com.tce.domain.model.ExecutionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ExecutionLogResponse {
    private UUID id;
    private UUID contractId;
    private Instant executionTimestamp;
    private ExecutionStatus status;
    private String details;
}
