package com.masu.user_service.controller;

import com.masu.user_service.dto.CreateProfileRequest;
import com.masu.user_service.dto.UpdateProfileRequest;
import com.masu.user_service.dto.UserResponse;
import com.masu.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createProfile(
            Authentication authentication,
            @Valid @RequestBody CreateProfileRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createProfile(authentication.getName(), request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                userService.getMyProfile(authentication.getName())
        );
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {

        return ResponseEntity.ok(
                userService.updateProfile(authentication.getName(), request)
        );
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getByUsername(
            Authentication authentication,
            @PathVariable String username
    ) {

        return ResponseEntity.ok(
                userService.getUserByUsername(username, requesterId(authentication))
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(
            Authentication authentication,
            @PathVariable String userId
    ) {

        return ResponseEntity.ok(
                userService.getUserById(userId, requesterId(authentication))
        );
    }

    private String requesterId(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authentication.getName();
    }
}
