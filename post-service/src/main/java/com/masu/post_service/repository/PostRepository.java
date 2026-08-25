package com.masu.post_service.repository;

import com.masu.post_service.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PostRepository
        extends MongoRepository<Post, String> {

    List<Post> findByUserIdOrderByCreatedAtDesc(
            String userId
    );

    List<Post> findAllByOrderByCreatedAtDesc();
}