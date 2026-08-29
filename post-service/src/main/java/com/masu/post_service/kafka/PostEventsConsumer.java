package com.masu.post_service.kafka;

import com.masu.events.CommentCreatedEvent;
import com.masu.events.CommentDeletedEvent;
import com.masu.events.LikeCreatedEvent;
import com.masu.events.LikeDeletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PostEventsConsumer {

    @KafkaListener(topics = "like.created", groupId = "post-service")
    public void consumeLikeCreated(LikeCreatedEvent event) {
        log.info(
                "like.created received: likeId={}, postId={}, userId={}, postOwnerId={}, createdAt={}",
                event.likeId(),
                event.postId(),
                event.userId(),
                event.postOwnerId(),
                event.createdAt()
        );
    }

    @KafkaListener(topics = "like.deleted", groupId = "post-service")
    public void consumeLikeDeleted(LikeDeletedEvent event) {
        log.info(
                "like.deleted received: likeId={}, postId={}, userId={}, postOwnerId={}, deletedAt={}",
                event.likeId(),
                event.postId(),
                event.userId(),
                event.postOwnerId(),
                event.deletedAt()
        );
    }

    @KafkaListener(topics = "comment.created", groupId = "post-service")
    public void consumeCommentCreated(CommentCreatedEvent event) {
        log.info(
                "comment.created received: commentId={}, postId={}, userId={}, postOwnerId={}, createdAt={}",
                event.commentId(),
                event.postId(),
                event.userId(),
                event.postOwnerId(),
                event.createdAt()
        );
    }

    @KafkaListener(topics = "comment.deleted", groupId = "post-service")
    public void consumeCommentDeleted(CommentDeletedEvent event) {
        log.info(
                "comment.deleted received: commentId={}, postId={}, userId={}, postOwnerId={}, createdAt={}",
                event.commentId(),
                event.postId(),
                event.userId(),
                event.postOwnerId(),
                event.createdAt()
        );
    }
}
