package com.masu.post_service.service;

import com.masu.post_service.dto.CreatePostRequest;
import com.masu.post_service.dto.PostResponse;
import com.masu.post_service.exception.PostNotFoundException;
import com.masu.post_service.model.Post;
import com.masu.post_service.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

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

        return PostResponse.from(
                postRepository.save(post)
        );
    }

    public List<PostResponse> getFeed() {
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(PostResponse::from)
                .toList();
    }

    public PostResponse getPost(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        return PostResponse.from(post);
    }

    public List<PostResponse> getPostsByUser(String userId) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(PostResponse::from)
                .toList();
    }
}