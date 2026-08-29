package com.masu.post_service.dto;

import com.masu.post_service.model.MediaType;
import com.masu.post_service.model.Post;

import java.time.Instant;

public record PostResponse(

        String id,

        String userId,

        String caption,

        String mediaUrl,

        MediaType mediaType,

        Instant createdAt,

        Instant updatedAt,
        long likeCount,
        long commentCount,
        boolean isLiked

) {

    public static PostResponse from(
            Post post,
            long likeCount,
            long commentCount,
            boolean isLiked
    ) {

        return new PostResponse(
                post.getId(),
                post.getUserId(),
                post.getCaption(),
                post.getMediaUrl(),
                post.getMediaType(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                likeCount,
                commentCount,
                isLiked
        );
    }
}