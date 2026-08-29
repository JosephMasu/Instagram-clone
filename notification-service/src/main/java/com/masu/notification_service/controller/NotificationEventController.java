package com.masu.notification_service.controller;

import com.masu.notification_service.dto.CommentNotifyRequest;
import com.masu.notification_service.dto.LikeNotifyRequest;
import com.masu.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/notifications/events")
@RequiredArgsConstructor
public class NotificationEventController {

    private final NotificationService notificationService;

    @PostMapping("/like")
    public ResponseEntity<Void> like(
            Authentication authentication,
            @Valid @RequestBody LikeNotifyRequest request
    ) {
        notificationService.createLikeNotification(
                authentication.getName(),
                request.postOwnerId(),
                request.postId(),
                Instant.now()
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/comment")
    public ResponseEntity<Void> comment(
            Authentication authentication,
            @Valid @RequestBody CommentNotifyRequest request
    ) {
        notificationService.createCommentNotifications(
                authentication.getName(),
                request.postOwnerId(),
                request.parentCommentAuthorId(),
                request.mentionedUserIds(),
                request.postId(),
                request.commentId(),
                request.parentCommentId(),
                Instant.now()
        );
        return ResponseEntity.noContent().build();
    }
}
