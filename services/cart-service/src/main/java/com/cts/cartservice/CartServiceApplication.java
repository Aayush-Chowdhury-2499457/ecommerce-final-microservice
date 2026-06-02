package com.cts.cartservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Spring Boot entry point for the cart-service application.
 */
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableDiscoveryClient
@EnableFeignClients
public class CartServiceApplication {

	/** Bootstraps the cart-service Spring application context. */
	public static void main(String[] args) {
		SpringApplication.run(CartServiceApplication.class, args);
	}

}
