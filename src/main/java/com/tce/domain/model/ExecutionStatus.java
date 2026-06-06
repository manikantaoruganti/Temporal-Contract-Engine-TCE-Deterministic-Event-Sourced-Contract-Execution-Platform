package com.tce.domain.model;

public enum ExecutionStatus {
    ATTEMPTED, // An attempt was made to execute the contract/rule
    SUCCESS,   // Execution was successful
    FAILED,    // Execution failed
    SKIPPED    // Execution was skipped (e.g., conditions not met)
}
