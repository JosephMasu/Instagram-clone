package com.masu.user_service.config;

import com.masu.events.UserCreatedEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;
import org.springframework.kafka.support.serializer.JsonSerializer;


@Configuration
public class KafkaConfig {

    /**
     * Creates the KafkaTemplate used by the User Service
     * to publish UserCreatedEvent messages to Kafka.
     */
    @Bean
    public KafkaTemplate<String, UserCreatedEvent> kafkaTemplate() {

        Map<String, Object> config = new HashMap<>();

        // Address of the Kafka broker
        config.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092"
        );

        // Kafka message keys are serialized as Strings
        config.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );
        // UserCreatedEvent objects are serialized as JSON
        config.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class
        );
        // The value serializer will be configured through Spring Boot
        // when Kafka is configured in application.yaml.

        return new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(config)
        );
    }
}