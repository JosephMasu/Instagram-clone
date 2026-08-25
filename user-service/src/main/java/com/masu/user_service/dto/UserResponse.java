package com.masu.user_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.masu.user_service.model.User;

import java.time.Instant;

public record UserResponse(
        String id,
        String authUserId,
        String username,
        String firstName,
        String lastName,
        String bio,
        String profilePictureUrl,
        @JsonProperty("isPrivate") boolean isPrivate,
        Instant createdAt,
        Instant updatedAt,
        long followerCount,
        long followingCount,
        Boolean isFollowing
) {

    public static UserResponse from(User user) {
        return from(user, 0, 0, null);
    }

    public static UserResponse from(
            User user,
            long followerCount,
            long followingCount,
            Boolean isFollowing
    ) {
        return new UserResponse(
                user.getId(),
                user.getAuthUserId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getBio(),
                user.getProfilePictureUrl(),
                user.isPrivate(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                followerCount,
                followingCount,
                isFollowing
        );
    }
}
