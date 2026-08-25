package com.masu.user_service.service;

import com.masu.user_service.dto.CreateProfileRequest;
import com.masu.user_service.dto.UpdateProfileRequest;
import com.masu.user_service.dto.UserResponse;
import com.masu.user_service.exception.UserNotFoundException;
import com.masu.user_service.model.User;
import com.masu.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FollowService followService;

    public UserResponse createProfile(
            String authUserId,
            CreateProfileRequest request
    ) {

        if (userRepository.existsByAuthUserId(authUserId)) {
            throw new IllegalStateException("Profile already exists");
        }

        String username = normalizeUsername(request.username());
        assertUsernameAvailable(username, null);

        Instant now = Instant.now();

        User user = User.builder()
                .authUserId(authUserId)
                .username(username)
                .usernameLower(username.toLowerCase(Locale.ROOT))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .bio(request.bio())
                .profilePictureUrl(request.profilePictureUrl())
                .isPrivate(request.isPrivate())
                .createdAt(now)
                .updatedAt(now)
                .build();

        return followService.toResponse(userRepository.save(user), authUserId);
    }

    public UserResponse getMyProfile(String authUserId) {
        return followService.toResponse(requireByAuthUserId(authUserId), authUserId);
    }

    public UserResponse getUserById(String userId, String requesterAuthUserId) {
        User user = userRepository.findById(userId)
                .or(() -> userRepository.findByAuthUserId(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return followService.toResponse(user, requesterAuthUserId);
    }

    public UserResponse getUserByUsername(String username, String requesterAuthUserId) {
        String usernameLower = normalizeUsername(username).toLowerCase(Locale.ROOT);

        User user = userRepository.findByUsernameLower(usernameLower)
                .or(() -> userRepository.findByUsernameIgnoreCase(usernameLower))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return followService.toResponse(user, requesterAuthUserId);
    }

    public UserResponse updateProfile(
            String authUserId,
            UpdateProfileRequest request
    ) {

        User user = requireByAuthUserId(authUserId);

        if (request.username() != null) {
            String username = normalizeUsername(request.username());
            if (!username.equals(user.getUsername())) {
                if (!username.equalsIgnoreCase(user.getUsername())) {
                    assertUsernameAvailable(username, user.getId());
                }
                user.setUsername(username);
                user.setUsernameLower(username.toLowerCase(Locale.ROOT));
            }
        }

        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }

        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }

        if (request.bio() != null) {
            user.setBio(request.bio());
        }

        if (request.profilePictureUrl() != null) {
            user.setProfilePictureUrl(request.profilePictureUrl());
        }

        if (request.isPrivate() != null) {
            user.setPrivate(request.isPrivate());
        }

        user.setUpdatedAt(Instant.now());

        return followService.toResponse(userRepository.save(user), authUserId);
    }

    private User requireByAuthUserId(String authUserId) {
        return userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new UserNotFoundException("User profile not found"));
    }

    private void assertUsernameAvailable(String username, String currentUserId) {
        String usernameLower = username.toLowerCase(Locale.ROOT);

        userRepository.findByUsernameLower(usernameLower)
                .or(() -> userRepository.findByUsernameIgnoreCase(username))
                .ifPresent(existing -> {
                    if (currentUserId == null || !existing.getId().equals(currentUserId)) {
                        throw new IllegalStateException("Username already exists");
                    }
                });
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username is required");
        }
        return username.trim();
    }
}
