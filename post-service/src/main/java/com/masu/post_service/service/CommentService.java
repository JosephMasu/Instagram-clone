package com.masu.post_service.service;

import com.masu.post_service.client.UserDirectoryClient;
import com.masu.post_service.comment.MentionParser;
import com.masu.post_service.dto.CommentResponse;
import com.masu.post_service.dto.CreateCommentRequest;
import com.masu.post_service.exception.PostNotFoundException;
import com.masu.post_service.kafka.PostEventProducer;
import com.masu.post_service.model.Comment;
import com.masu.post_service.repository.CommentRepository;
import com.masu.post_service.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final PostService postService;
    private final PostEventProducer postEventProducer;
    private final UserDirectoryClient userDirectoryClient;

    public CommentResponse createComment(
            String postId,
            CreateCommentRequest request,
            Authentication authentication
    ) {

        var post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        String userId = authentication.getName();
        String parentCommentAuthorId = null;
        String parentCommentId = blankToNull(request.parentCommentId());

        if (parentCommentId != null) {
            Comment parent = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
            if (!postId.equals(parent.getPostId())) {
                throw new IllegalArgumentException("Parent comment does not belong to this post");
            }
            parentCommentAuthorId = parent.getUserId();
        }

        Instant now = Instant.now();

        Comment comment = Comment.builder()
                .postId(postId)
                .userId(userId)
                .parentCommentId(parentCommentId)
                .text(request.text())
                .createdAt(now)
                .updatedAt(now)
                .build();

        Comment savedComment = commentRepository.save(comment);
        postService.refreshEngagementCounts(postId);
        try {
            postEventProducer.publishCommentCreated(
                    savedComment,
                    post.getUserId(),
                    parentCommentAuthorId,
                    resolveMentionedUserIds(request.text(), userId)
            );
        } catch (Exception exception) {
            log.error(
                    "Failed to publish comment.created commentId={} postId={}",
                    savedComment.getId(),
                    postId,
                    exception
            );
        }

        return CommentResponse.from(savedComment);
    }

    public List<CommentResponse> getComments(String postId) {
        return commentRepository
                .findByPostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    public void deleteComment(
            String commentId,
            Authentication authentication
    ) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new RuntimeException("Comment not found")
                );

        String userId = authentication.getName();

        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException(
                    "You are not allowed to delete this comment"
            );
        }

        String postOwnerId = postRepository.findById(comment.getPostId())
                .map(post -> post.getUserId())
                .orElse(null);

        commentRepository.delete(comment);
        postService.refreshEngagementCounts(comment.getPostId());
        try {
            postEventProducer.publishCommentDeleted(comment, postOwnerId);
        } catch (Exception exception) {
            log.error(
                    "Failed to publish comment.deleted commentId={} postId={}",
                    comment.getId(),
                    comment.getPostId(),
                    exception
            );
        }
    }

    private List<String> resolveMentionedUserIds(String text, String actorUserId) {
        Set<String> mentioned = new LinkedHashSet<>();
        for (String username : MentionParser.usernames(text)) {
            userDirectoryClient.findAuthUserIdByUsername(username)
                    .filter(authUserId -> !authUserId.equals(actorUserId))
                    .ifPresent(mentioned::add);
        }
        return new ArrayList<>(mentioned);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
