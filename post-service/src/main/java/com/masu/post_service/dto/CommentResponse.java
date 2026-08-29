package com.masu.post_service.dto;

import com.masu.post_service.client.UserProfileLookup;
import com.masu.post_service.model.Comment;

import java.time.Instant;

public record CommentResponse(
        String id,
        String postId,
        String userId,
        String username,
        String profilePictureUrl,
        String parentCommentId,
        String text,
        Instant createdAt,
        Instant updatedAt
) {
    public static CommentResponse from(Comment comment) {
        return from(comment, null);
    }

    public static CommentResponse from(Comment comment, UserProfileLookup user) {
        return new CommentResponse(
                comment.getId(),
                comment.getPostId(),
                comment.getUserId(),
                user == null ? null : user.username(),
                user == null ? null : user.profilePictureUrl(),
                comment.getParentCommentId(),
                comment.getText(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
