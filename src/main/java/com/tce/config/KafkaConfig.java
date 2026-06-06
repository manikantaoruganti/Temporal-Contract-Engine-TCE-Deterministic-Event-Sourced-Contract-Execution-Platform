package com.tce.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String CONTRACT_EVENTS_TOPIC = "contract-events";
    public static final String RULE_EVENTS_TOPIC = "rule-events";
    public static final String EXECUTION_EVENTS_TOPIC = "execution-events";

    @Bean
    public NewTopic contractEventsTopic() {
        return TopicBuilder.name(CONTRACT_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ruleEventsTopic() {
        return TopicBuilder.name(RULE_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic executionEventsTopic() {
        return TopicBuilder.name(EXECUTION_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
