package com.tce.api.controller;

import com.tce.application.service.ReplayEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Operations", description = "Administrative operations for auditing and replaying state")
public class AdminController {

    private final ReplayEngineService replayEngineService;
    private final com.tce.domain.repository.DeadLetterExecutionRepository deadLetterExecutionRepository;

    @Operation(summary = "Replay a contract's event log to audit state consistency",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Replay audit report executed successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReplayEngineService.ReplayReport.class))),
                    @ApiResponse(responseCode = "404", description = "Contract not found")
            })
    @PostMapping("/replay/{contractId}")
    public ResponseEntity<ReplayEngineService.ReplayReport> replayContract(@PathVariable UUID contractId) {
        ReplayEngineService.ReplayReport report = replayEngineService.replayContract(contractId);
        return ResponseEntity.ok(report);
    }

    @Operation(summary = "Get dead letter executions",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of dead letter executions",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.tce.domain.model.DeadLetterExecution.class)))
            })
    @GetMapping("/dead-letters")
    public ResponseEntity<java.util.List<com.tce.domain.model.DeadLetterExecution>> getDeadLetters() {
        java.util.List<com.tce.domain.model.DeadLetterExecution> deadLetters = deadLetterExecutionRepository.findAll();
        return ResponseEntity.ok(deadLetters);
    }
}
