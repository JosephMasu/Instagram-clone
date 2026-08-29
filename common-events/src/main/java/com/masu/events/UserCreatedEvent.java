package com.masu.events;

public record UserCreatedEvent(
        String userId,
        String authUserId,
        String username,
        String firstName,
        String lastName
) {
}