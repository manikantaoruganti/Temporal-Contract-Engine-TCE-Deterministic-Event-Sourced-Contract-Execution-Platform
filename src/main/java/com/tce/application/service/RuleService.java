package com.tce.application.service;

import com.tce.api.dto.RuleCreateRequest;
import com.tce.api.dto.RuleResponse;
import com.tce.domain.model.Contract;
import com.tce.domain.model.Rule;
import com.tce.domain.repository.ContractRepository;
import com.tce.domain.repository.RuleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tce.domain.event.RuleAddedEvent;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleService {

    private final RuleRepository ruleRepository;
    private final ContractRepository contractRepository;
    private final RuleValidationService ruleValidationService;
    private final EventPublisher eventPublisher;

    @Transactional
    @CacheEvict(value = {"rules", "contracts"}, allEntries = true) // Evict contract cache as rules are part of it
    public RuleResponse createRule(RuleCreateRequest request) {
        log.info("Creating new rule for contract ID: {}", request.getContractId());

        // Validate the Rule DSL expression syntax
        ruleValidationService.validate(request.getConditionExpression());

        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new EntityNotFoundException("Contract not found with ID: " + request.getContractId()));

        Rule rule = Rule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .conditionExpression(request.getConditionExpression())
                .actionPayload(request.getActionPayload())
                .priority(request.getPriority())
                .contract(contract)
                .build();

        Rule savedRule = ruleRepository.save(rule);
        log.debug("Rule saved with ID: {}", savedRule.getId());

        // Add rule to contract's rule list (managed by JPA, but good to ensure consistency)
        contract.getRules().add(savedRule);
        contractRepository.save(contract); // Persist the change to the contract

        // Publish RuleAddedEvent which will be stored and sent to Kafka
        eventPublisher.publish(new RuleAddedEvent(
                contract.getId(),
                savedRule.getId(),
                savedRule.getName(),
                savedRule.getConditionExpression(),
                savedRule.getActionPayload(),
                savedRule.getPriority(),
                Instant.now()
        ));

        return mapToRuleResponse(savedRule);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "rules", key = "#id")
    public RuleResponse getRuleById(UUID id) {
        log.debug("Fetching rule by ID: {}", id);
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rule not found with ID: " + id));
        return mapToRuleResponse(rule);
    }

    private RuleResponse mapToRuleResponse(Rule rule) {
        return RuleResponse.builder()
                .id(rule.getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .conditionExpression(rule.getConditionExpression())
                .actionPayload(rule.getActionPayload())
                .priority(rule.getPriority())
                .contractId(rule.getContract().getId())
                .creationDate(rule.getCreationDate())
                .lastModifiedDate(rule.getLastModifiedDate())
                .build();
    }
}
