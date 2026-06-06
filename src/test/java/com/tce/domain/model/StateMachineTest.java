package com.tce.domain.model;

import com.tce.api.exception.IllegalStateTransitionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StateMachineTest {

    @Test
    public void testValidTransitions() {
        // PENDING -> ELIGIBLE, EXECUTING, CANCELLED
        assertDoesNotThrow(() -> ContractStateMachine.validateTransition(ContractStatus.PENDING, ContractStatus.ELIGIBLE));
        assertDoesNotThrow(() -> ContractStateMachine.validateTransition(ContractStatus.PENDING, ContractStatus.EXECUTING));
        assertDoesNotThrow(() -> ContractStateMachine.validateTransition(ContractStatus.PENDING, ContractStatus.CANCELLED));

        // ELIGIBLE -> EXECUTING, FAILED, CANCELLED
        assertDoesNotThrow(() -> ContractStateMachine.validateTransition(ContractStatus.ELIGIBLE, ContractStatus.EXECUTING));
        assertDoesNotThrow(() -> ContractStateMachine.validateTransition(ContractStatus.ELIGIBLE, ContractStatus.FAILED));
        assertDoesNotThrow(() -> ContractStateMachine.validateTransition(ContractStatus.ELIGIBLE, ContractStatus.CANCELLED));

        // EXECUTING -> COMPLETED, FAILED
        assertDoesNotThrow(() -> ContractStateMachine.validateTransition(ContractStatus.EXECUTING, ContractStatus.COMPLETED));
        assertDoesNotThrow(() -> ContractStateMachine.validateTransition(ContractStatus.EXECUTING, ContractStatus.FAILED));

        // FAILED -> ELIGIBLE, EXECUTING, CANCELLED
        assertDoesNotThrow(() -> ContractStateMachine.validateTransition(ContractStatus.FAILED, ContractStatus.ELIGIBLE));
        assertDoesNotThrow(() -> ContractStateMachine.validateTransition(ContractStatus.FAILED, ContractStatus.EXECUTING));
        assertDoesNotThrow(() -> ContractStateMachine.validateTransition(ContractStatus.FAILED, ContractStatus.CANCELLED));
    }

    @Test
    public void testSelfTransitionIsAllowed() {
        assertDoesNotThrow(() -> ContractStateMachine.validateTransition(ContractStatus.PENDING, ContractStatus.PENDING));
        assertDoesNotThrow(() -> ContractStateMachine.validateTransition(ContractStatus.COMPLETED, ContractStatus.COMPLETED));
    }

    @Test
    public void testTerminalStatesCannotTransition() {
        // COMPLETED cannot transition
        assertThrows(IllegalStateTransitionException.class, () -> 
                ContractStateMachine.validateTransition(ContractStatus.COMPLETED, ContractStatus.PENDING));
        assertThrows(IllegalStateTransitionException.class, () -> 
                ContractStateMachine.validateTransition(ContractStatus.COMPLETED, ContractStatus.EXECUTING));

        // CANCELLED cannot transition
        assertThrows(IllegalStateTransitionException.class, () -> 
                ContractStateMachine.validateTransition(ContractStatus.CANCELLED, ContractStatus.ELIGIBLE));
    }

    @Test
    public void testInvalidTransitions() {
        // PENDING cannot go directly to COMPLETED
        assertThrows(IllegalStateTransitionException.class, () -> 
                ContractStateMachine.validateTransition(ContractStatus.PENDING, ContractStatus.COMPLETED));

        // ELIGIBLE cannot go directly to COMPLETED
        assertThrows(IllegalStateTransitionException.class, () -> 
                ContractStateMachine.validateTransition(ContractStatus.ELIGIBLE, ContractStatus.COMPLETED));

        // EXECUTING cannot go back to PENDING or ELIGIBLE directly
        assertThrows(IllegalStateTransitionException.class, () -> 
                ContractStateMachine.validateTransition(ContractStatus.EXECUTING, ContractStatus.PENDING));
    }
}
