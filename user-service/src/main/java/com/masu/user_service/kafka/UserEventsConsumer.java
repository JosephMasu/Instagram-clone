package com.masu.user_service.kafka;

import com.masu.events.UserFollowedEvent;
import com.masu.events.UserUnfollowedEvent;
import com.masu.user_service.service.UserFollowEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@KafkaListener(
        id = "user-events",
        topics = "user.events",
        groupId = "user-service"
)
public class UserEventsConsumer {

    private final UserFollowEventService userFollowEventService;

    @KafkaHandler
    public void consumeUserFollowed(UserFollowedEvent event) {
        log.info(
                "USER_FOLLOWED received: followerId={}, followingId={}, createdAt={}",
                event.followerId(),
                event.followingId(),
                event.createdAt()
        );
        userFollowEventService.handleUserFollowed(event);
    }

    @KafkaHandler
    public void consumeUserUnfollowed(UserUnfollowedEvent event) {
        log.info(
                "USER_UNFOLLOWED received: followerId={}, followingId={}, createdAt={}",
                event.followerId(),
                event.followingId(),
                event.createdAt()
        );
        userFollowEventService.handleUserUnfollowed(event);
    }
}
