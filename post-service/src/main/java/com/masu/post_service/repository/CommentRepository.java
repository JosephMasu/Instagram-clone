package com.masu.post_service.repository;

import com.masu.post_service.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {

    List<Comment> findByPostIdOrderByCreatedAtAsc(String postId);

    List<Comment> findByUserIdOrderByCreatedAtAsc(String userId);

    void deleteByParentCommentId(String parentCommentId);

    long countByPostId(String postId);
}