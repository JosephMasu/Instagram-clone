package com.masu.post_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "posts")
public class Post {

    @Id
    private String id;

    private String userId;

    private String caption;

    @Builder.Default
    private long likeCount = 0;

    @Builder.Default
    private long commentCount = 0;

    private String mediaUrl;

    private MediaType mediaType;

    private Instant createdAt;

    private Instant updatedAt;
}