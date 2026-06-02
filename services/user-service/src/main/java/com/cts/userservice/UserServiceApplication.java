package com.cts.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Spring Boot entry point for the user-service application.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class UserServiceApplication {

	/** Boots the Spring application context. */
	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
}