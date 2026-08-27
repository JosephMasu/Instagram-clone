package com.masu.user_service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String authUserId;

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true, sparse = true)
    private String usernameLower;

    private String firstName;

    private String lastName;

    private String bio;

    private String profilePictureUrl;

    @JsonProperty("isPrivate")
    private boolean isPrivate;

    @Builder.Default
    private long followerCount = 0;

    @Builder.Default
    private long followingCount = 0;

    private Instant createdAt;

    private Instant updatedAt;
}