package com.masu.events;

import java.time.Instant;

public record CommentCreatedEvent(
        String commentId,
        String postId,
        String userId,
        String postOwnerId,
        Instant createdAt
) {
}
