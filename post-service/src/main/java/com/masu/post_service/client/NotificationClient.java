package com.masu.post_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationClient {

    private final RestClient notificationServiceRestClient;

    public void notifyLike(String postOwnerId, String postId) {
        post("/api/v1/notifications/events/like", Map.of(
                "postOwnerId", postOwnerId,
                "postId", postId
        ));
    }

    public void notifyComment(
            String postOwnerId,
            String postId,
            String commentId,
            String parentCommentId,
            String parentCommentAuthorId,
            List<String> mentionedUserIds
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("postOwnerId", postOwnerId);
        body.put("postId", postId);
        body.put("commentId", commentId);
        body.put("parentCommentId", parentCommentId);
        body.put("parentCommentAuthorId", parentCommentAuthorId);
        body.put("mentionedUserIds", mentionedUserIds == null ? List.of() : mentionedUserIds);
        post("/api/v1/notifications/events/comment", body);
    }

    private void post(String path, Map<String, Object> body) {
        String authorization = currentAuthorization();
        if (authorization == null) {
            log.warn("Skipping notification {} because the request has no access token", path);
            return;
        }
        try {
            notificationServiceRestClient.post()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception exception) {
            log.error("Failed to notify {}", path, exception);
        }
    }

    private String currentAuthorization() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return null;
        }
        String header = servletAttributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        return header == null || header.isBlank() ? null : header;
    }
}
