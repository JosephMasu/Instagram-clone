package com.masu.user_service.kafka;

import com.masu.events.UserCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventProducer {

    // Kafka topic where user creation events are published
    private static final String USER_CREATED_TOPIC = "user.created";

    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    public UserEventProducer(
            KafkaTemplate<String, UserCreatedEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes a UserCreatedEvent to the user.created Kafka topic.
     *
     * The user's ID is used as the Kafka message key so that
     * events belonging to the same user can be consistently grouped.
     */
    public void publishUserCreated(UserCreatedEvent event) {

        kafkaTemplate.send(
                USER_CREATED_TOPIC,
                event.userId(),
                event
        );
    }
}