package com.masu.user_service;

import com.masu.user_service.config.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class UserServiceApplication {

	public static void main(String[] args) {
		EnvLoader.load("user-service");
		SpringApplication.run(UserServiceApplication.class, args);
	}

}
