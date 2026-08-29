package com.masu.post_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDirectoryClient {

    private final RestClient userServiceRestClient;

    public Optional<String> findAuthUserIdByUsername(String username) {
        try {
            UserProfileLookup profile = userServiceRestClient.get()
                    .uri("/api/v1/users/username/{username}", username)
                    .retrieve()
                    .body(UserProfileLookup.class);

            if (profile == null || profile.authUserId() == null || profile.authUserId().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(profile.authUserId());
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                return Optional.empty();
            }
            log.warn("Failed to resolve mention username={}", username, exception);
            return Optional.empty();
        } catch (RestClientException exception) {
            log.warn("Failed to resolve mention username={}", username, exception);
            return Optional.empty();
        }
    }
}
