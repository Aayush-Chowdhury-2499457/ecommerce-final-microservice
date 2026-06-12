package com.cts.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Spring Boot entry point for the Product Service microservice.
 * <p>
 * Bootstraps the product-catalog application, registers the running instance
 * with the service-discovery registry, and enables JPA auditing so that
 * creation and modification metadata is populated automatically on persisted
 * entities. The {@code auditorAware} bean referenced here is supplied by
 * {@link com.cts.productservice.config.AuditingConfig}.
 *
 * @since 1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class ProductServiceApplication {

    /**
     * Application launch hook that boots the Spring {@code ApplicationContext}.
     *
     * @param args command-line arguments forwarded to Spring Boot at startup
     */
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
