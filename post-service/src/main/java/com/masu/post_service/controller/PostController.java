package com.masu.post_service.controller;

import com.masu.post_service.dto.CreatePostRequest;
import com.masu.post_service.dto.PostResponse;
import com.masu.post_service.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            Authentication authentication,
            @Valid @RequestBody CreatePostRequest request
    ) {

        String userId = authentication.getName();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        postService.createPost(
                                userId,
                                request
                        )
                );
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getFeed( Authentication authentication) {
        String currentUserId =
                authentication.getName();
        return ResponseEntity.ok(postService.getFeed(currentUserId));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(Authentication authentication,
            @PathVariable String postId
    ) {
        String currentUserId= authentication.getName();
        return ResponseEntity.ok(postService.getPost(postId, currentUserId));
    }
}