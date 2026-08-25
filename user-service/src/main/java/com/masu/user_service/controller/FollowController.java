package com.masu.user_service.controller;

import com.masu.user_service.dto.UserResponse;
import com.masu.user_service.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{userId}/follow")
    public ResponseEntity<Void> follow(
            Authentication authentication,
            @PathVariable String userId
    ) {
        followService.follow(authentication.getName(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{userId}/follow")
    public ResponseEntity<Void> unfollow(
            Authentication authentication,
            @PathVariable String userId
    ) {
        followService.unfollow(authentication.getName(), userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<UserResponse>> getFollowers(
            @PathVariable String userId
    ) {
        return ResponseEntity.ok(followService.getFollowers(userId));
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<List<UserResponse>> getFollowing(
            @PathVariable String userId
    ) {
        return ResponseEntity.ok(followService.getFollowing(userId));
    }
}
