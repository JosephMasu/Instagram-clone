package com.masu.notification_service;

import com.masu.notification_service.config.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class NotificationServiceApplication {

	public static void main(String[] args) {
		EnvLoader.load("notification-service");
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

}
