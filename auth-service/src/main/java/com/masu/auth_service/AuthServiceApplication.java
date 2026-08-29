package com.masu.auth_service;

import com.masu.auth_service.config.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthServiceApplication {

	public static void main(String[] args) {
		EnvLoader.load("auth-service");
		SpringApplication.run(AuthServiceApplication.class, args);
	}
}
