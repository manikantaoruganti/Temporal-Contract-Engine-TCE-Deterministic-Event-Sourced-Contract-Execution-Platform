package com.tce.domain.model;

import com.tce.api.exception.IllegalStateTransitionException;

public class ContractStateMachine {

    public static void validateTransition(ContractStatus from, ContractStatus to) {
        if (from == to) {
            return;
        }

        boolean valid = switch (from) {
            case PENDING -> to == ContractStatus.ELIGIBLE || to == ContractStatus.EXECUTING || to == ContractStatus.CANCELLED;
            case ELIGIBLE -> to == ContractStatus.EXECUTING || to == ContractStatus.FAILED || to == ContractStatus.CANCELLED;
            case EXECUTING -> to == ContractStatus.COMPLETED || to == ContractStatus.FAILED;
            case FAILED -> to == ContractStatus.ELIGIBLE || to == ContractStatus.EXECUTING || to == ContractStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false; // Terminal states
        };

        if (!valid) {
            throw new IllegalStateTransitionException(from, to);
        }
    }
}
