package com.masu.post_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(

        @NotBlank(message = "Comment cannot be empty")
        @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
        String text,

        String parentCommentId

) {}