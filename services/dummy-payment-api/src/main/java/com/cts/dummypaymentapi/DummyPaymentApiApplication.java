package com.cts.dummypaymentapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Spring Boot entry point for the dummy payment API service.
 */
@SpringBootApplication
@EnableJpaAuditing
public class DummyPaymentApiApplication {

    /**
     * Boots the Spring application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(DummyPaymentApiApplication.class, args);
    }
}