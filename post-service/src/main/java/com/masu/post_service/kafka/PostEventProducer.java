package com.masu.post_service.kafka;

import com.masu.events.CommentCreatedEvent;
import com.masu.events.CommentDeletedEvent;
import com.masu.events.LikeCreatedEvent;
import com.masu.events.LikeDeletedEvent;
import com.masu.post_service.model.Comment;
import com.masu.post_service.model.Like;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostEventProducer {

    public static final String LIKE_CREATED_TOPIC = "like.created";
    public static final String LIKE_DELETED_TOPIC = "like.deleted";
    public static final String COMMENT_CREATED_TOPIC = "comment.created";
    public static final String COMMENT_DELETED_TOPIC = "comment.deleted";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishLikeCreated(Like like, String postOwnerId) {
        send(
                LIKE_CREATED_TOPIC,
                like.getPostId(),
                new LikeCreatedEvent(
                        like.getId(),
                        like.getPostId(),
                        like.getUserId(),
                        postOwnerId,
                        like.getCreatedAt()
                )
        );
    }

    public void publishLikeDeleted(Like like, String postOwnerId) {
        send(
                LIKE_DELETED_TOPIC,
                like.getPostId(),
                new LikeDeletedEvent(
                        like.getId(),
                        like.getPostId(),
                        like.getUserId(),
                        postOwnerId,
                        Instant.now()
                )
        );
    }

    public void publishCommentCreated(
            Comment comment,
            String postOwnerId,
            String parentCommentAuthorId,
            java.util.List<String> mentionedUserIds
    ) {
        send(
                COMMENT_CREATED_TOPIC,
                comment.getPostId(),
                new CommentCreatedEvent(
                        comment.getId(),
                        comment.getPostId(),
                        comment.getUserId(),
                        postOwnerId,
                        comment.getParentCommentId(),
                        parentCommentAuthorId,
                        mentionedUserIds,
                        comment.getCreatedAt()
                )
        );
    }

    public void publishCommentDeleted(Comment comment, String postOwnerId) {
        send(
                COMMENT_DELETED_TOPIC,
                comment.getPostId(),
                new CommentDeletedEvent(
                        comment.getId(),
                        comment.getPostId(),
                        comment.getUserId(),
                        postOwnerId,
                        Instant.now()
                )
        );
    }

    private void send(String topic, String key, Object event) {
        kafkaTemplate.send(topic, key, event).whenComplete((result, exception) -> {
            if (exception != null) {
                log.error("Failed to publish {} key={}", topic, key, exception);
            }
        });
    }
}
