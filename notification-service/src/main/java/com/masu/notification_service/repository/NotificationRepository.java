package com.masu.notification_service.repository;

import com.masu.notification_service.model.Notification;
import com.masu.notification_service.model.NotificationType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository
        extends MongoRepository<Notification, String> {

    List<Notification> findByRecipientUserIdOrderByCreatedAtDesc(String recipientUserId);

    long countByRecipientUserIdAndReadFalse(String recipientUserId);

    Optional<Notification> findByIdAndRecipientUserId(String id, String recipientUserId);

    List<Notification> findByRecipientUserIdAndReadFalse(String recipientUserId);

    boolean existsByTypeAndActorUserIdAndRecipientUserId(
            NotificationType type,
            String actorUserId,
            String recipientUserId
    );

    boolean existsByTypeAndActorUserIdAndPostId(
            NotificationType type,
            String actorUserId,
            String postId
    );

    void deleteByTypeAndActorUserIdAndRecipientUserId(
            NotificationType type,
            String actorUserId,
            String recipientUserId
    );

    void deleteByTypeAndActorUserIdAndPostId(
            NotificationType type,
            String actorUserId,
            String postId
    );

    boolean existsByTypeAndActorUserIdAndCommentId(
            NotificationType type,
            String actorUserId,
            String commentId
    );

    void deleteByCommentId(String commentId);
}
