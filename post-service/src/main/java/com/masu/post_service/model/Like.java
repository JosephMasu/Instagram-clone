package com.masu.post_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

// Each Like is stored as its own MongoDB document.
// This is better than storing a huge list of user IDs inside Post.
@Document(collection = "likes")

// Prevents the same user from liking the same post more than once.
// The combination of userId + postId must be unique.
@CompoundIndex(
        name = "user_post_unique",
        def = "{'userId': 1, 'postId': 1}",
        unique = true
)
public class Like {

    @Id
    private String id;

    // The user who performed the like.
    // This comes from the "sub" claim in our JWT.
    private String userId;

    // The post that was liked.
    private String postId;

    // Useful later for sorting likes and publishing Kafka events.
    private Instant createdAt;
}