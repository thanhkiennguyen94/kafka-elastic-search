package com.example.userservice.config;

import com.example.commonservice.util.ConstantTopic;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic userCreatedTopic() {
        return TopicBuilder.name(ConstantTopic.CREATE_USER_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}