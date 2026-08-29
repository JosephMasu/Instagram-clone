package com.masu.notification_service.dto;

import com.masu.notification_service.model.Notification;
import com.masu.notification_service.model.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        String id,
        String recipientUserId,
        String actorUserId,
        NotificationType type,
        String postId,
        String commentId,
        String parentCommentId,
        boolean read,
        Instant createdAt
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipientUserId(),
                notification.getActorUserId(),
                notification.getType(),
                notification.getPostId(),
                notification.getCommentId(),
                notification.getParentCommentId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
