package com.masu.user_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @Size(min = 3, max = 30)
        @Pattern(regexp = "^[a-zA-Z0-9._]+$")
        String username,

        @Size(max = 50)
        String firstName,

        @Size(max = 50)
        String lastName,

        @Size(max = 150)
        String bio,

        String profilePictureUrl,

        @JsonProperty("isPrivate")
        Boolean isPrivate
) {
}
