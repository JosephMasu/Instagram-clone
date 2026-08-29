package com.masu.notification_service.service;

import com.masu.notification_service.dto.NotificationResponse;
import com.masu.notification_service.dto.UnreadCountResponse;
import com.masu.notification_service.exception.NotificationNotFoundException;
import com.masu.notification_service.model.Notification;
import com.masu.notification_service.model.NotificationType;
import com.masu.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationResponse> listForUser(String recipientUserId) {
        return notificationRepository
                .findByRecipientUserIdOrderByCreatedAtDesc(recipientUserId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    public UnreadCountResponse unreadCount(String recipientUserId) {
        return new UnreadCountResponse(
                notificationRepository.countByRecipientUserIdAndReadFalse(recipientUserId)
        );
    }

    public NotificationResponse markRead(String notificationId, String recipientUserId) {
        Notification notification = notificationRepository
                .findByIdAndRecipientUserId(notificationId, recipientUserId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }

        return NotificationResponse.from(notification);
    }

    public void markAllRead(String recipientUserId) {
        List<Notification> unread = notificationRepository
                .findByRecipientUserIdAndReadFalse(recipientUserId);
        unread.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unread);
    }

    public void createFollowNotification(String actorUserId, String recipientUserId, Instant createdAt) {
        if (shouldSkip(actorUserId, recipientUserId)) {
            return;
        }
        if (notificationRepository.existsByTypeAndActorUserIdAndRecipientUserId(
                NotificationType.FOLLOW,
                actorUserId,
                recipientUserId
        )) {
            return;
        }
        save(recipientUserId, actorUserId, NotificationType.FOLLOW, null, null, createdAt);
    }

    public void deleteFollowNotification(String actorUserId, String recipientUserId) {
        notificationRepository.deleteByTypeAndActorUserIdAndRecipientUserId(
                NotificationType.FOLLOW,
                actorUserId,
                recipientUserId
        );
    }

    public void createLikeNotification(
            String actorUserId,
            String recipientUserId,
            String postId,
            Instant createdAt
    ) {
        if (shouldSkip(actorUserId, recipientUserId) || recipientUserId == null || recipientUserId.isBlank()) {
            return;
        }
        if (notificationRepository.existsByTypeAndActorUserIdAndPostId(
                NotificationType.LIKE,
                actorUserId,
                postId
        )) {
            return;
        }
        save(recipientUserId, actorUserId, NotificationType.LIKE, postId, null, createdAt);
    }

    public void deleteLikeNotification(String actorUserId, String postId) {
        notificationRepository.deleteByTypeAndActorUserIdAndPostId(
                NotificationType.LIKE,
                actorUserId,
                postId
        );
    }

    public void createCommentNotification(
            String actorUserId,
            String recipientUserId,
            String postId,
            String commentId,
            Instant createdAt
    ) {
        if (shouldSkip(actorUserId, recipientUserId) || recipientUserId == null || recipientUserId.isBlank()) {
            return;
        }
        save(recipientUserId, actorUserId, NotificationType.COMMENT, postId, commentId, createdAt);
    }

    public void deleteCommentNotification(String commentId) {
        if (commentId == null || commentId.isBlank()) {
            return;
        }
        notificationRepository.deleteByTypeAndCommentId(NotificationType.COMMENT, commentId);
    }

    private boolean shouldSkip(String actorUserId, String recipientUserId) {
        return actorUserId == null
                || recipientUserId == null
                || actorUserId.equals(recipientUserId);
    }

    private void save(
            String recipientUserId,
            String actorUserId,
            NotificationType type,
            String postId,
            String commentId,
            Instant createdAt
    ) {
        Notification notification = Notification.builder()
                .recipientUserId(recipientUserId)
                .actorUserId(actorUserId)
                .type(type)
                .postId(postId)
                .commentId(commentId)
                .read(false)
                .createdAt(createdAt != null ? createdAt : Instant.now())
                .build();
        notificationRepository.save(notification);
        log.info(
                "Notification stored type={} recipient={} actor={} postId={}",
                type,
                recipientUserId,
                actorUserId,
                postId
        );
    }
}
