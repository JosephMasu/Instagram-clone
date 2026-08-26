package com.masu.user_service.kafka;

import com.masu.events.UserCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UserCreatedConsumer {

    /**
     * Listens for UserCreatedEvent messages published
     * to the user.created Kafka topic.
     */
    @KafkaListener(
            topics = "user.created",
            groupId = "user-service"
    )
    public void consumeUserCreated(UserCreatedEvent event) {

        System.out.println(
                "UserCreatedEvent received: " + event
        );
    }
}