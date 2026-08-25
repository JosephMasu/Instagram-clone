package com.masu.post_service.repository;

import com.masu.post_service.model.Like;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

// Spring Data automatically implements these database queries for us.
// We only need to describe the method names.
public interface LikeRepository extends MongoRepository<Like, String> {

    // Used before creating a like so we can prevent duplicates.
    boolean existsByUserIdAndPostId(
            String userId,
            String postId
    );

    // Used when removing a like.
    Optional<Like> findByUserIdAndPostId(
            String userId,
            String postId
    );

    // Used later when returning the number of likes on a post.
    long countByPostId(String postId);
}