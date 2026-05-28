package org.example.dummypaymentapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DummyPaymentApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(DummyPaymentApiApplication.class, args);
    }
}