package com.masu.post_service.service;

import com.masu.post_service.dto.CreatePostRequest;
import com.masu.post_service.dto.PostResponse;
import com.masu.post_service.exception.PostNotFoundException;
import com.masu.post_service.kafka.PostEventProducer;
import com.masu.post_service.model.Like;
import com.masu.post_service.model.Post;
import com.masu.post_service.repository.CommentRepository;
import com.masu.post_service.repository.LikeRepository;
import com.masu.post_service.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PostEventProducer postEventProducer;

    public PostResponse createPost(
            String userId,
            CreatePostRequest request
    ) {

        Instant now = Instant.now();

        Post post = Post.builder()
                .userId(userId)
                .caption(request.caption())
                .mediaUrl(request.mediaUrl())
                .mediaType(request.mediaType())
                .likeCount(0)
                .commentCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Post savedPost = postRepository.save(post);
        return toResponse(savedPost, userId);

    }

    public List<PostResponse> getFeed(String currentUserId) {
        return postRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(post -> toResponse(post, currentUserId))
                .toList();
    }

    public PostResponse getPost(String postId, String currentUserId) {
        Post post = requirePost(postId);
        return toResponse(post, currentUserId);
    }

    public List<PostResponse> getPostsByUser(String userId, String currentUserId) {
        return postRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(post -> toResponse(post, currentUserId))
                .toList();
    }

    public void likePost(
            String userId,
            String postId
    ) {

        // First make sure the post actually exists.
        // Otherwise we could create a Like pointing to nothing.
        requirePost(postId);

        if (likeRepository.existsByUserIdAndPostId(
                userId,
                postId
        )) {
            throw new IllegalStateException("Post already liked");
        }

        Like like = Like.builder()
                .userId(userId)
                .postId(postId)
                .createdAt(Instant.now())
                .build();

        Like savedLike = likeRepository.save(like);
        refreshEngagementCounts(postId);
        try {
            postEventProducer.publishLikeCreated(savedLike);
        } catch (Exception exception) {
            log.error(
                    "Failed to publish like.created likeId={} postId={}",
                    savedLike.getId(),
                    postId,
                    exception
            );
        }
    }

    public void unlikePost(
            String userId,
            String postId
    ) {

        // Find the specific like belonging to this user.
        //
        // This is important: we don't simply delete by postId,
        // because many different users can like the same post.
        requirePost(postId);

        Like like = likeRepository
                .findByUserIdAndPostId(
                        userId,
                        postId
                )
                .orElseThrow(() ->
                        new IllegalStateException("Post is not liked")
                );

        likeRepository.delete(like);
        refreshEngagementCounts(postId);
        try {
            postEventProducer.publishLikeDeleted(like);
        } catch (Exception exception) {
            log.error(
                    "Failed to publish like.deleted likeId={} postId={}",
                    like.getId(),
                    postId,
                    exception
            );
        }
    }

    public void refreshEngagementCounts(String postId) {
        applyEngagementCounts(requirePost(postId));
    }

    private Post requirePost(String postId) {
        return postRepository
                .findById(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found")
                );
    }

    private void applyEngagementCounts(Post post) {
        long likeCount = likeRepository.countByPostId(post.getId());
        long commentCount = commentRepository.countByPostId(post.getId());

        if (post.getLikeCount() == likeCount
                && post.getCommentCount() == commentCount) {
            return;
        }

        post.setLikeCount(likeCount);
        post.setCommentCount(commentCount);
        postRepository.save(post);
    }

    private PostResponse toResponse(Post post, String currentUserId) {
        applyEngagementCounts(post);

        return PostResponse.from(
                post,
                post.getLikeCount(),
                post.getCommentCount(),
                likeRepository.existsByUserIdAndPostId(
                        currentUserId,
                        post.getId()
                )
        );
    }
}