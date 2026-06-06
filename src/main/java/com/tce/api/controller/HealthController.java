package com.tce.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Application health check endpoint")
public class HealthController {

    @Operation(summary = "Check application health",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Application is healthy")
            })
    @GetMapping
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Temporal Contract Engine is running!");
    }
}
