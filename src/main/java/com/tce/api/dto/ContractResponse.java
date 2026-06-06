package com.tce.api.dto;

import com.tce.domain.model.ContractStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ContractResponse {
    private UUID id;
    private String name;
    private String description;
    private ContractStatus status;
    private Instant effectiveDate;
    private Instant expirationDate;
    private Instant creationDate;
    private Instant lastModifiedDate;
    private List<RuleResponse> rules;
}
