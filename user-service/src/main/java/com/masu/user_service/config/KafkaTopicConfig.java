package com.masu.user_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic userCreatedTopic() {
        return TopicBuilder.name("user.created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userFollowedTopic() {
        return TopicBuilder.name("user.followed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userUnfollowedTopic() {
        return TopicBuilder.name("user.unfollowed")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
