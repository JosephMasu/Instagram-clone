package com.masu.post_service.controller;

import com.masu.post_service.dto.PostResponse;
import com.masu.post_service.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserPostController {

    private final PostService postService;

    @GetMapping("/{userId}/posts")
    public ResponseEntity<List<PostResponse>> getPostsByUser( Authentication authentication,
            @PathVariable String userId
    ) {
        String currentUserId = authentication.getName();
        return ResponseEntity.ok(postService.getPostsByUser(userId, currentUserId));
    }
}
