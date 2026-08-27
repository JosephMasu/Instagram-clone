package com.masu.auth_service.dto;

import com.masu.auth_service.Model.User;

public record UserResponse(
        String id,
        String username,
        String email
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId().toHexString(),
                user.getUsername(),
                user.getEmail()
        );
    }
}
