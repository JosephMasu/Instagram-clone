package com.masu.notification_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private String recipientUserId;

    private String actorUserId;

    private NotificationType type;

    private String postId;

    private String commentId;

    private String parentCommentId;

    private boolean read;

    private Instant createdAt;
}