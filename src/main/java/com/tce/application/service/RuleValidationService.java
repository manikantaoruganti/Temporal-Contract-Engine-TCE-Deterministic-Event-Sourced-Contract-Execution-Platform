package com.tce.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleValidationService {

    private final RuleParser ruleParser;

    /**
     * Validates whether a rule expression is syntactically correct.
     * Throws IllegalArgumentException if parsing fails.
     */
    public void validate(String conditionExpression) {
        if (conditionExpression == null || conditionExpression.trim().isEmpty()) {
            throw new IllegalArgumentException("Rule condition expression cannot be empty");
        }
        try {
            ruleParser.parse(conditionExpression);
            log.debug("Rule validation successful for expression: '{}'", conditionExpression);
        } catch (Exception e) {
            log.error("Rule validation failed for expression: '{}'. Error: {}", conditionExpression, e.getMessage());
            throw new IllegalArgumentException("Invalid rule expression syntax: " + e.getMessage(), e);
        }
    }
}
