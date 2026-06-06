package com.tce.api.controller;

import com.tce.api.dto.ContractCreateRequest;
import com.tce.api.dto.ContractResponse;
import com.tce.application.service.ContractService;
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

import com.tce.application.service.ContractExecutionService;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
@Tag(name = "Contracts", description = "API for managing temporal contracts")
public class ContractController {

    private final ContractService contractService;
    private final ContractExecutionService contractExecutionService;
    private final com.tce.application.service.ExecutionLogService executionLogService;
    private final com.tce.application.service.EventStoreService eventStoreService;

    @Operation(summary = "Create a new contract",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Contract created successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ContractResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid contract data")
            })
    @PostMapping
    public ResponseEntity<ContractResponse> createContract(@Valid @RequestBody ContractCreateRequest request) {
        ContractResponse response = contractService.createContract(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get a contract by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Contract found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ContractResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Contract not found")
            })
    @GetMapping("/{id}")
    public ResponseEntity<ContractResponse> getContractById(@PathVariable UUID id) {
        ContractResponse response = contractService.getContractById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all contracts",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of all contracts",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ContractResponse.class)))
            })
    @GetMapping
    public ResponseEntity<List<ContractResponse>> getAllContracts() {
        List<ContractResponse> contracts = contractService.getAllContracts();
        return ResponseEntity.ok(contracts);
    }

    @Operation(summary = "Manually execute a contract with custom facts",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Execution triggered successfully"),
                    @ApiResponse(responseCode = "404", description = "Contract not found"),
                    @ApiResponse(responseCode = "400", description = "Illegal state transition")
            })
    @PostMapping("/{id}/execute")
    public ResponseEntity<Void> executeContract(@PathVariable UUID id, @RequestBody(required = false) Map<String, Object> facts) {
        contractExecutionService.executeContract(id, facts);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Cancel a contract",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Contract cancelled successfully"),
                    @ApiResponse(responseCode = "404", description = "Contract not found"),
                    @ApiResponse(responseCode = "400", description = "Illegal state transition")
            })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelContract(@PathVariable UUID id) {
        contractService.updateContractStatus(id, com.tce.domain.model.ContractStatus.CANCELLED);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get execution logs for a contract")
    @GetMapping("/{id}/execution-logs")
    public ResponseEntity<List<com.tce.domain.model.ExecutionLog>> getExecutionLogs(@PathVariable UUID id) {
        List<com.tce.domain.model.ExecutionLog> logs = executionLogService.getExecutionLogs(id);
        return ResponseEntity.ok(logs);
    }

    @Operation(summary = "Get events for a contract")
    @GetMapping("/{id}/events")
    public ResponseEntity<List<com.tce.domain.model.EventStore>> getEvents(@PathVariable UUID id) {
        List<com.tce.domain.model.EventStore> events = eventStoreService.getEventsForAggregate(id);
        return ResponseEntity.ok(events);
    }
}
