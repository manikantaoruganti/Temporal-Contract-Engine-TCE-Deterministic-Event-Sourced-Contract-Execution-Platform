package com.tce.api.exception;

import com.tce.domain.model.ContractStatus;

public class IllegalStateTransitionException extends RuntimeException {
    public IllegalStateTransitionException(ContractStatus from, ContractStatus to) {
        super(String.format("Illegal state transition from %s to %s", from, to));
    }
}
