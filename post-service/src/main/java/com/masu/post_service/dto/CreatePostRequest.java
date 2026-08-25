package com.masu.post_service.dto;

import com.masu.post_service.model.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(

        @NotBlank
        @Size(max = 2200)
        String caption,

        @NotBlank
        String mediaUrl,

        @NotNull
        MediaType mediaType
) {
}