package com.tce.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleCreateRequest {
    @NotBlank(message = "Rule name cannot be empty")
    @Size(max = 255, message = "Rule name cannot exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Rule description cannot exceed 1000 characters")
    private String description;

    @NotBlank(message = "Condition expression cannot be empty")
    private String conditionExpression; // e.g., SpEL expression

    @NotBlank(message = "Action payload cannot be empty")
    private String actionPayload; // JSON string for action details

    @NotNull(message = "Priority cannot be null")
    private Integer priority;

    @NotNull(message = "Contract ID cannot be null")
    private UUID contractId;
}
