package com.masu.post_service;

import com.masu.post_service.config.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PostServiceApplication {

	public static void main(String[] args) {
		EnvLoader.load("post-service");
		SpringApplication.run(PostServiceApplication.class, args);
	}

}
