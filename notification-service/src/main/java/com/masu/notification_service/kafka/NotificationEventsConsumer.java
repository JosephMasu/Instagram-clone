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
    }

    @KafkaListener(topics = "user.unfollowed", groupId = "notification-service")
    public void consumeUserUnfollowed(UserUnfollowedEvent event) {
        log.info(
                "user.unfollowed received: followerId={}, followingId={}",
                event.followerId(),
                event.followingId()
        );
        notificationService.deleteFollowNotification(
                event.followerId(),
                event.followingId()
        );
    }

    @KafkaListener(topics = "like.created", groupId = "notification-service")
    public void consumeLikeCreated(LikeCreatedEvent event) {
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
    }

    @KafkaListener(topics = "like.deleted", groupId = "notification-service")
    public void consumeLikeDeleted(LikeDeletedEvent event) {
        log.info(
                "like.deleted received: likeId={}, postId={}, userId={}",
                event.likeId(),
                event.postId(),
                event.userId()
        );
        notificationService.deleteLikeNotification(event.userId(), event.postId());
    }

    @KafkaListener(topics = "comment.created", groupId = "notification-service")
    public void consumeCommentCreated(CommentCreatedEvent event) {
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
    }

    @KafkaListener(topics = "comment.deleted", groupId = "notification-service")
    public void consumeCommentDeleted(CommentDeletedEvent event) {
        log.info(
                "comment.deleted received: commentId={}, postId={}, userId={}",
                event.commentId(),
                event.postId(),
                event.userId()
        );
        notificationService.deleteCommentNotification(event.commentId());
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
