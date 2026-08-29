package com.masu.events;

import java.time.Instant;

public record CommentDeletedEvent(
        String commentId,
        String postId,
        String userId,
        Instant createdAt
) {
}
