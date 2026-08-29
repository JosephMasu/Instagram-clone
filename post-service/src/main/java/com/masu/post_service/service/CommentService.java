package com.masu.post_service.service;

import com.masu.post_service.client.NotificationClient;
import com.masu.post_service.client.UserDirectoryClient;
import com.masu.post_service.client.UserProfileLookup;
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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
    private final NotificationClient notificationClient;

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
        List<String> mentionedUserIds = resolveMentionedUserIds(request.text(), userId);
        try {
            postEventProducer.publishCommentCreated(
                    savedComment,
                    post.getUserId(),
                    parentCommentAuthorId,
                    mentionedUserIds
            );
        } catch (Exception exception) {
            log.error(
                    "Failed to publish comment.created commentId={} postId={}",
                    savedComment.getId(),
                    postId,
                    exception
            );
        }
        notificationClient.notifyComment(
                post.getUserId(),
                postId,
                savedComment.getId(),
                parentCommentId,
                parentCommentAuthorId,
                mentionedUserIds
        );

        return toResponse(savedComment, userId);
    }

    public List<CommentResponse> getComments(String postId) {
        Map<String, UserProfileLookup> cache = new HashMap<>();
        return commentRepository
                .findByPostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(comment -> toResponse(comment, comment.getUserId(), cache))
                .toList();
    }

    private CommentResponse toResponse(Comment comment, String userId) {
        return toResponse(comment, userId, new HashMap<>());
    }

    private CommentResponse toResponse(
            Comment comment,
            String userId,
            Map<String, UserProfileLookup> cache
    ) {
        var profile = cache.computeIfAbsent(
                userId,
                id -> userDirectoryClient.findByUserId(id).orElse(null)
        );
        return CommentResponse.from(comment, profile);
    }

    public void deleteComment(
            String commentId,
            Authentication authentication
    ) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Comment not found")
                );

        String userId = authentication.getName();
        boolean commentAuthor = comment.getUserId().equals(userId);
        boolean postAuthor = postRepository.findById(comment.getPostId())
                .map(post -> userId.equals(post.getUserId()))
                .orElse(false);

        if (!commentAuthor && !postAuthor) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not allowed to delete this comment"
            );
        }

        String postOwnerId = postRepository.findById(comment.getPostId())
                .map(post -> post.getUserId())
                .orElse(null);

        commentRepository.deleteByParentCommentId(commentId);
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
