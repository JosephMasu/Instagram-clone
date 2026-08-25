package com.masu.user_service.service;

import com.masu.user_service.dto.UserResponse;
import com.masu.user_service.exception.UserNotFoundException;
import com.masu.user_service.model.Follow;
import com.masu.user_service.model.User;
import com.masu.user_service.repository.FollowRepository;
import com.masu.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public void follow(
            String followerAuthUserId,
            String targetProfileId
    ) {

        User targetUser = userRepository.findById(targetProfileId)
                .orElseThrow(() ->
                        new IllegalStateException("User profile not found"));

        String followingAuthUserId = targetUser.getAuthUserId();

        if (followerAuthUserId.equals(followingAuthUserId)) {
            throw new IllegalStateException(
                    "You cannot follow yourself"
            );
        }

        if (followRepository.existsByFollowerIdAndFollowingId(
                followerAuthUserId,
                followingAuthUserId
        )) {
            throw new IllegalStateException(
                    "Already following this user"
            );
        }

        Follow follow = Follow.builder()
                .followerId(followerAuthUserId)
                .followingId(followingAuthUserId)
                .createdAt(Instant.now())
                .build();

        followRepository.save(follow);
    }
    public void unfollow(
            String followerAuthUserId,
            String targetProfileId
    ) {

        User targetUser = userRepository.findById(targetProfileId)
                .orElseThrow(() ->
                        new IllegalStateException("User profile not found"));

        String followingAuthUserId = targetUser.getAuthUserId();

        Follow follow = followRepository
                .findByFollowerIdAndFollowingId(
                        followerAuthUserId,
                        followingAuthUserId
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "You are not following this user"
                        ));

        followRepository.delete(follow);
    }
    public List<UserResponse> getFollowers(String userId) {
        User user = requireUser(userId);

        return followRepository.findByFollowingId(user.getAuthUserId())
                .stream()
                .map(Follow::getFollowerId)
                .map(authUserId -> userRepository.findByAuthUserId(authUserId).orElse(null))
                .filter(Objects::nonNull)
                .map(this::toResponse)
                .toList();
    }

    public List<UserResponse> getFollowing(String userId) {
        User user = requireUser(userId);

        return followRepository.findByFollowerId(user.getAuthUserId())
                .stream()
                .map(Follow::getFollowingId)
                .map(authUserId -> userRepository.findByAuthUserId(authUserId).orElse(null))
                .filter(Objects::nonNull)
                .map(this::toResponse)
                .toList();
    }

    public UserResponse toResponse(User user) {
        return toResponse(user, null);
    }

    public UserResponse toResponse(User user, String requesterAuthUserId) {
        long followerCount = followRepository.countByFollowingId(user.getAuthUserId());
        long followingCount = followRepository.countByFollowerId(user.getAuthUserId());
        Boolean isFollowing = null;

        if (requesterAuthUserId != null && !requesterAuthUserId.equals(user.getAuthUserId())) {
            isFollowing = followRepository.existsByFollowerIdAndFollowingId(
                    requesterAuthUserId,
                    user.getAuthUserId()
            );
        }

        return UserResponse.from(user, followerCount, followingCount, isFollowing);
    }

    private void requireProfile(String authUserId) {
        if (!userRepository.existsByAuthUserId(authUserId)) {
            throw new UserNotFoundException("User profile not found");
        }
    }

    private User requireUser(String userId) {
        return userRepository.findById(userId)
                .or(() -> userRepository.findByAuthUserId(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
