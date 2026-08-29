package com.masu.events;

public record UserUnfollowedEvent(
        String followerId,
        String followingId,
        String createdAt
) {}
