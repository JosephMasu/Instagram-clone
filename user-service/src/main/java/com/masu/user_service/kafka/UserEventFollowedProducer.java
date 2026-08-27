package com.masu.user_service.kafka;

import com.masu.events.UserFollowedEvent;
import com.masu.events.UserUnfollowedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class UserEventFollowedProducer {

    private static final String USER_EVENTS_TOPIC = "user.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserFollowed(
           String followerId,
           String followingId
   ){
       UserFollowedEvent event = new UserFollowedEvent(
               followerId,
               followingId,
               Instant.now().toString()
       );
       kafkaTemplate.send(
               USER_EVENTS_TOPIC,
               followerId,
               event);
   }

    public void publishUserUnfollowed(
            String followerId,
            String followingId
    ) {
        UserUnfollowedEvent event = new UserUnfollowedEvent(
                followerId,
                followingId,
                Instant.now().toString()
        );
        kafkaTemplate.send(
                USER_EVENTS_TOPIC,
                followerId,
                event
        );
    }
}
