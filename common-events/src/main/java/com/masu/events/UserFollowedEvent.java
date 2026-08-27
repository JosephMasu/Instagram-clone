package com.masu.events;

public record UserFollowedEvent(
        String followerId,
        String followingId,
        String createdAt
) {}
