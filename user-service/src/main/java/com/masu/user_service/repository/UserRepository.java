package com.masu.user_service.repository;

import com.masu.user_service.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByAuthUserId(String authUserId);

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByUsernameLower(String usernameLower);

    boolean existsByAuthUserId(String authUserId);
}