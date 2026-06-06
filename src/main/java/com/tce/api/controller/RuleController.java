package com.tce.api.controller;

import com.tce.api.dto.RuleCreateRequest;
import com.tce.api.dto.RuleResponse;
import com.tce.application.service.RuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
@Tag(name = "Rules", description = "API for managing rules associated with contracts")
public class RuleController {

    private final RuleService ruleService;

    @Operation(summary = "Create a new rule for a contract",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Rule created successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RuleResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid rule data or contract not found")
            })
    @PostMapping
    public ResponseEntity<RuleResponse> createRule(@Valid @RequestBody RuleCreateRequest request) {
        RuleResponse response = ruleService.createRule(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get a rule by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Rule found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RuleResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Rule not found")
            })
    @GetMapping("/{id}")
    public ResponseEntity<RuleResponse> getRuleById(@PathVariable UUID id) {
        RuleResponse response = ruleService.getRuleById(id);
        return ResponseEntity.ok(response);
    }
}
