package com.masu.notification_service.dto;

import jakarta.validation.constraints.NotBlank;

public record LikeNotifyRequest(
        @NotBlank String postOwnerId,
        @NotBlank String postId
) {
}
