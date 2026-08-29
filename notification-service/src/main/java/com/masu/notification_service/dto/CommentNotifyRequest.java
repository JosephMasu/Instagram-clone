package com.masu.notification_service.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CommentNotifyRequest(
        @NotBlank String postOwnerId,
        @NotBlank String postId,
        @NotBlank String commentId,
        String parentCommentId,
        String parentCommentAuthorId,
        List<String> mentionedUserIds
) {
}
