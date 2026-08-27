package com.masu.user_service.service;

import com.masu.events.UserFollowedEvent;
import com.masu.events.UserUnfollowedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserFollowEventService {

    public void handleUserFollowed(UserFollowedEvent event) {
        log.debug(
                "Follow event applied in Mongo already: followerId={}, followingId={}",
                event.followerId(),
                event.followingId()
        );
    }

    public void handleUserUnfollowed(UserUnfollowedEvent event) {
        log.debug(
                "Unfollow event applied in Mongo already: followerId={}, followingId={}",
                event.followerId(),
                event.followingId()
        );
    }
}
