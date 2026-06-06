package com.tce.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class RuleResponse {
    private UUID id;
    private String name;
    private String description;
    private String conditionExpression;
    private String actionPayload;
    private Integer priority;
    private UUID contractId;
    private Instant creationDate;
    private Instant lastModifiedDate;
}
