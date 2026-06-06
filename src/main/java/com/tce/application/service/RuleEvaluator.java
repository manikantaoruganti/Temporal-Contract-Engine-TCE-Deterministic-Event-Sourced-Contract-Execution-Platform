package com.tce.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleEvaluator {

    private final RuleParser ruleParser;
    private final ConcurrentHashMap<String, RuleParser.Expression> compilationCache = new ConcurrentHashMap<>();

    /**
     * Evaluates a DSL rule condition against context facts.
     */
    public boolean evaluate(String conditionExpression, Map<String, Object> facts) {
        if (conditionExpression == null || conditionExpression.trim().isEmpty()) {
            return true; // Empty rule is considered always matching
        }

        RuleParser.Expression expression = compilationCache.computeIfAbsent(conditionExpression, exprStr -> {
            log.debug("Compiling rule expression (Cache Miss): '{}'", exprStr);
            return ruleParser.parse(exprStr);
        });

        try {
            boolean result = expression.evaluate(facts);
            log.debug("Evaluated rule condition '{}' with facts {} -> {}", conditionExpression, facts, result);
            return result;
        } catch (Exception e) {
            log.error("Error evaluating expression '{}' with context: {}", conditionExpression, facts, e);
            throw new RuntimeException("Rule evaluation exception", e);
        }
    }

    /**
     * Clears compile caches (e.g. if memory cleanup required).
     */
    public void clearCache() {
        compilationCache.clear();
    }
}
