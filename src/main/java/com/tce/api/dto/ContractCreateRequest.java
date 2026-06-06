package com.tce.api.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractCreateRequest {
    @NotBlank(message = "Contract name cannot be empty")
    @Size(max = 255, message = "Contract name cannot exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Contract description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Effective date cannot be null")
    @FutureOrPresent(message = "Effective date must be in the present or future")
    private Instant effectiveDate;

    @FutureOrPresent(message = "Expiration date must be in the present or future")
    private Instant expirationDate;

    private List<UUID> ruleIds; // Optional: associate existing rules
}
