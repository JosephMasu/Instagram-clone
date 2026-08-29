package com.masu.post_service.dto;

import com.masu.post_service.model.Comment;

import java.time.Instant;

public record CommentResponse(
        String id,
        String postId,
        String userId,
        String text,
        Instant createdAt,
        Instant updatedAt
) {
    public static CommentResponse from(Comment comment){
        return new CommentResponse(
                comment.getId(),
                comment.getPostId(),
                comment.getUserId(),
                comment.getText(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
