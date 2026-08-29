package com.masu.post_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic likeCreatedTopic() {
        return TopicBuilder.name("like.created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic likeDeletedTopic() {
        return TopicBuilder.name("like.deleted")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic commentCreatedTopic() {
        return TopicBuilder.name("comment.created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic commentDeletedTopic() {
        return TopicBuilder.name("comment.deleted")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
