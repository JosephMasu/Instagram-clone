package com.masu.post_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient userServiceRestClient(
            @Value("${user-service.url}") String userServiceUrl
    ) {
        return RestClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }

    @Bean
    public RestClient notificationServiceRestClient(
            @Value("${notification-service.url}") String notificationServiceUrl
    ) {
        return RestClient.builder()
                .baseUrl(notificationServiceUrl)
                .build();
    }
}
