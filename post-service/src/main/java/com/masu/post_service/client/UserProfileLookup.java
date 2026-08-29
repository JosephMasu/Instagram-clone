package com.masu.post_service.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserProfileLookup(
        String id,
        String authUserId,
        String username,
        String profilePictureUrl
) {
}
