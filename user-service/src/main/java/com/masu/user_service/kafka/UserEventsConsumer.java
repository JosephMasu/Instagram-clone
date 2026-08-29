package com.masu.user_service.kafka;

import com.masu.events.UserFollowedEvent;
import com.masu.events.UserUnfollowedEvent;
import com.masu.user_service.service.UserFollowEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserEventsConsumer {

    private final UserFollowEventService userFollowEventService;

    @KafkaListener(topics = "user.followed", groupId = "user-service")
    public void consumeUserFollowed(UserFollowedEvent event) {
        log.info(
                "user.followed received: followerId={}, followingId={}, createdAt={}",
                event.followerId(),
                event.followingId(),
                event.createdAt()
        );
        userFollowEventService.handleUserFollowed(event);
    }

    @KafkaListener(topics = "user.unfollowed", groupId = "user-service")
    public void consumeUserUnfollowed(UserUnfollowedEvent event) {
        log.info(
                "user.unfollowed received: followerId={}, followingId={}, createdAt={}",
                event.followerId(),
                event.followingId(),
                event.createdAt()
        );
        userFollowEventService.handleUserUnfollowed(event);
    }
}
