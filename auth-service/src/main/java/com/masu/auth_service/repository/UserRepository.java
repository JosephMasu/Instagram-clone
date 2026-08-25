package com.masu.auth_service.repository;

import com.masu.auth_service.Model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends MongoRepository<User, ObjectId> {

        Optional<User> findByEmail(String email);

        Optional<User> findByUsername(String username);

        boolean existsByEmail(String email);

        boolean existsByUsername(String username);
    }
