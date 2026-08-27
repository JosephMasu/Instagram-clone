package com.masu.post_service.service;

import com.masu.post_service.dto.CreatePostRequest;
import com.masu.post_service.dto.PostResponse;
import com.masu.post_service.exception.PostNotFoundException;
import com.masu.post_service.model.Like;
import com.masu.post_service.model.Post;
import com.masu.post_service.repository.LikeRepository;
import com.masu.post_service.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;

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
                .createdAt(now)
                .updatedAt(now)
                .build();

        Post savedPost = postRepository.save(post);
        return PostResponse.from(savedPost, 0, false);

    }

    public List<PostResponse> getFeed(String currentUserId) {
        return postRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(post -> {

                    long likeCount =
                            likeRepository.countByPostId(
                                    post.getId()
                            );

                    boolean isLiked =
                            likeRepository.existsByUserIdAndPostId(
                                    currentUserId,
                                    post.getId()
                            );

                    return PostResponse.from(
                            post,
                            likeCount,
                            isLiked
                    );
                })
                .toList();
    }

    public PostResponse getPost(String postId, String currentUserId) {
        Post post = postRepository
                .findById(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found")
                );

        long likeCount =
                likeRepository.countByPostId(postId);

        boolean isLiked =
                likeRepository.existsByUserIdAndPostId(
                        currentUserId,
                        postId
                );

        return PostResponse.from(
                post,
                likeCount,
                isLiked
        );
    }

    public List<PostResponse> getPostsByUser(String userId, String currentUserId) {
        return postRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(post -> PostResponse.from(
                        post,
                        likeRepository.countByPostId(post.getId()),
                        likeRepository.existsByUserIdAndPostId(
                                currentUserId,
                                post.getId()
                        )
                ))
                .toList();
    }

    public void likePost(
            String userId,
            String postId
    ) {

        // First make sure the post actually exists.
        // Otherwise we could create a Like pointing to nothing.
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException("Post not found");
        }

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

        // Store the like in the "likes" collection.
        likeRepository.save(like);
    }

    public void unlikePost(
            String userId,
            String postId
    ) {

        // Find the specific like belonging to this user.
        //
        // This is important: we don't simply delete by postId,
        // because many different users can like the same post.
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException("Post not found");
        }

        Like like = likeRepository
                .findByUserIdAndPostId(
                        userId,
                        postId
                )
                .orElseThrow(() ->
                        new IllegalStateException("Post is not liked")
                );

        // Delete only this user's like.
        likeRepository.delete(like);
    }
}