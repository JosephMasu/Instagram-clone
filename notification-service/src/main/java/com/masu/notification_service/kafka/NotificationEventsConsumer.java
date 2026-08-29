package com.masu.notification_service.kafka;

import com.masu.events.CommentCreatedEvent;
import com.masu.events.CommentDeletedEvent;
import com.masu.events.LikeCreatedEvent;
import com.masu.events.LikeDeletedEvent;
import com.masu.events.UserFollowedEvent;
import com.masu.events.UserUnfollowedEvent;
import com.masu.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationEventsConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "user.followed", groupId = "notification-service")
    public void consumeUserFollowed(UserFollowedEvent event) {
        try {
            log.info(
                    "user.followed received: followerId={}, followingId={}",
                    event.followerId(),
                    event.followingId()
            );
            notificationService.createFollowNotification(
                    event.followerId(),
                    event.followingId(),
                    parseInstant(event.createdAt())
            );
        } catch (Exception exception) {
            log.error("Failed to handle user.followed", exception);
        }
    }

    @KafkaListener(topics = "user.unfollowed", groupId = "notification-service")
    public void consumeUserUnfollowed(UserUnfollowedEvent event) {
        try {
            log.info(
                    "user.unfollowed received: followerId={}, followingId={}",
                    event.followerId(),
                    event.followingId()
            );
            notificationService.deleteFollowNotification(
                    event.followerId(),
                    event.followingId()
            );
        } catch (Exception exception) {
            log.error("Failed to handle user.unfollowed", exception);
        }
    }

    @KafkaListener(topics = "like.created", groupId = "notification-service")
    public void consumeLikeCreated(LikeCreatedEvent event) {
        try {
            log.info(
                    "like.created received: likeId={}, postId={}, userId={}, postOwnerId={}",
                    event.likeId(),
                    event.postId(),
                    event.userId(),
                    event.postOwnerId()
            );
            notificationService.createLikeNotification(
                    event.userId(),
                    event.postOwnerId(),
                    event.postId(),
                    event.createdAt()
            );
        } catch (Exception exception) {
            log.error("Failed to handle like.created", exception);
        }
    }

    @KafkaListener(topics = "like.deleted", groupId = "notification-service")
    public void consumeLikeDeleted(LikeDeletedEvent event) {
        try {
            log.info(
                    "like.deleted received: likeId={}, postId={}, userId={}",
                    event.likeId(),
                    event.postId(),
                    event.userId()
            );
            notificationService.deleteLikeNotification(event.userId(), event.postId());
        } catch (Exception exception) {
            log.error("Failed to handle like.deleted", exception);
        }
    }

    @KafkaListener(topics = "comment.created", groupId = "notification-service")
    public void consumeCommentCreated(CommentCreatedEvent event) {
        try {
            log.info(
                    "comment.created received: commentId={}, postId={}, userId={}, postOwnerId={}",
                    event.commentId(),
                    event.postId(),
                    event.userId(),
                    event.postOwnerId()
            );
            notificationService.createCommentNotifications(
                    event.userId(),
                    event.postOwnerId(),
                    event.parentCommentAuthorId(),
                    event.mentionedUserIds(),
                    event.postId(),
                    event.commentId(),
                    event.parentCommentId(),
                    event.createdAt()
            );
        } catch (Exception exception) {
            log.error("Failed to handle comment.created", exception);
        }
    }

    @KafkaListener(topics = "comment.deleted", groupId = "notification-service")
    public void consumeCommentDeleted(CommentDeletedEvent event) {
        try {
            log.info(
                    "comment.deleted received: commentId={}, postId={}, userId={}",
                    event.commentId(),
                    event.postId(),
                    event.userId()
            );
            notificationService.deleteCommentNotification(event.commentId());
        } catch (Exception exception) {
            log.error("Failed to handle comment.deleted", exception);
        }
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return Instant.now();
        }
        try {
            return Instant.parse(value);
        } catch (Exception exception) {
            return Instant.now();
        }
    }
}
