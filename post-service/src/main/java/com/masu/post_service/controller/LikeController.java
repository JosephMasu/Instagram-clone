package com.masu.post_service.controller;

import com.masu.post_service.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class LikeController {

    private final PostService postService;

    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> likePost(
            Authentication authentication,
            @PathVariable String postId
    ) {

        // Spring Security populated Authentication after
        // our JWT filter validated the access token.
        //
        // authentication.getName() is the JWT "sub",
        // which is our authUserId.
        String userId = authentication.getName();

        postService.likePost(userId, postId);

        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<Void> unlikePost(
            Authentication authentication,
            @PathVariable String postId
    ) {

        // We use the authenticated user rather than accepting
        // a userId from the request body. This prevents a user
        // from pretending to unlike another user's like.
        String userId = authentication.getName();

        postService.unlikePost(userId, postId);

        return ResponseEntity.noContent().build();
    }
}