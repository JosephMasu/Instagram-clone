package com.masu.events;

import java.time.Instant;

public record LikeCreatedEvent(
        String likeId,
        String postId,
        String userId,
        String postOwnerId,
        Instant createdAt
) {
}
