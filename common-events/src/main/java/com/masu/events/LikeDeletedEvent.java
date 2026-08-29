package com.masu.events;

import java.time.Instant;

public record LikeDeletedEvent(
        String likeId,
        String postId,
        String userId,
        Instant deletedAt
) {
}
