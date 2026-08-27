package com.masu.user_service.kafka;

import com.masu.events.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventProducer {

    private static final String USER_CREATED_TOPIC = "user.created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserCreated(UserCreatedEvent event) {
        kafkaTemplate.send(
                USER_CREATED_TOPIC,
                event.userId(),
                event
        );
    }
}
