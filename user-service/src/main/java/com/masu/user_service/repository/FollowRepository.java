package com.masu.user_service.repository;

import com.masu.user_service.model.Follow;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends MongoRepository<Follow, String> {

    boolean existsByFollowerIdAndFollowingId(
            String followerId,
            String followingId
    );

    Optional<Follow> findByFollowerIdAndFollowingId(
            String followerId,
            String followingId
    );

    List<Follow> findByFollowingId(String followingId);

    List<Follow> findByFollowerId(String followerId);

    long countByFollowerId(String followerId);

    long countByFollowingId(String followingId);
}
