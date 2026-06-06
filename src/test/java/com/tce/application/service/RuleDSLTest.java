package com.tce.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RuleDSLTest {

    private RuleParser parser;
    private RuleEvaluator evaluator;
    private RuleValidationService validationService;

    @BeforeEach
    public void setUp() {
        this.parser = new RuleParser();
        this.evaluator = new RuleEvaluator(parser);
        this.validationService = new RuleValidationService(parser);
    }

    @Test
    public void testSimpleComparisons() {
        Map<String, Object> context = Map.of(
                "balance", 1500,
                "risk_score", 25,
                "attempt_count", 3
        );

        assertTrue(evaluator.evaluate("balance > 1000", context));
        assertFalse(evaluator.evaluate("balance < 1000", context));
        assertTrue(evaluator.evaluate("risk_score <= 30", context));
        assertTrue(evaluator.evaluate("attempt_count == 3", context));
        assertTrue(evaluator.evaluate("attempt_count != 5", context));
    }

    @Test
    public void testLogicalAnd() {
        Map<String, Object> context = Map.of(
                "balance", 1200,
                "risk_score", 20
        );

        assertTrue(evaluator.evaluate("balance > 1000 AND risk_score < 30", context));
        assertFalse(evaluator.evaluate("balance > 1500 AND risk_score < 30", context));
        assertFalse(evaluator.evaluate("balance > 1000 AND risk_score > 30", context));
    }

    @Test
    public void testLogicalOr() {
        Map<String, Object> context = Map.of(
                "balance", 900,
                "risk_score", 20
        );

        assertTrue(evaluator.evaluate("balance > 1000 OR risk_score < 30", context));
        assertTrue(evaluator.evaluate("balance < 1000 OR risk_score > 30", context));
        assertFalse(evaluator.evaluate("balance > 1000 OR risk_score > 30", context));
    }

    @Test
    public void testCompoundExpressions() {
        Map<String, Object> context = Map.of(
                "balance", 1200,
                "risk_score", 25,
                "attempt_count", 2
        );

        // Precedence: AND evaluates before OR (balance > 1000 AND risk_score < 30) = true. OR (attempt_count > 5) = true.
        assertTrue(evaluator.evaluate("balance > 1000 AND risk_score < 30 OR attempt_count > 5", context));
        
        // Paren groups (attempt_count > 5 OR risk_score < 30) = true. AND (balance < 1000) = false.
        assertFalse(evaluator.evaluate("(attempt_count > 5 OR risk_score < 30) AND balance < 1000", context));
    }

    @Test
    public void testValidationSuccess() {
        assertDoesNotThrow(() -> validationService.validate("balance > 1000"));
        assertDoesNotThrow(() -> validationService.validate("balance > 1000 AND risk_score < 30"));
        assertDoesNotThrow(() -> validationService.validate("(balance > 1000 OR risk_score < 30) AND attempt_count <= 5"));
    }

    @Test
    public void testValidationFailure() {
        assertThrows(IllegalArgumentException.class, () -> validationService.validate(""));
        assertThrows(IllegalArgumentException.class, () -> validationService.validate("balance >"));
        assertThrows(IllegalArgumentException.class, () -> validationService.validate("balance > AND risk_score < 30"));
        assertThrows(IllegalArgumentException.class, () -> validationService.validate("balance > 1000 OR"));
    }
}
