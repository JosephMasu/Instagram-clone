package com.masu.events;

import java.time.Instant;
import java.util.List;

public record CommentCreatedEvent(
        String commentId,
        String postId,
        String userId,
        String postOwnerId,
        String parentCommentId,
        String parentCommentAuthorId,
        List<String> mentionedUserIds,
        Instant createdAt
) {
}
