package com.masu.auth_service.dto;

import org.bson.types.ObjectId;

public record UserResponse(
        ObjectId id,
        String username,
        String email
) {
}
