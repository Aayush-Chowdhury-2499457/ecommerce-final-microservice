package com.cts.reviewservice.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Configures JPA auditing to derive the current auditor from the request user header.
 */
@Slf4j
@Configuration
public class AuditingConfig {

    /** Resolves the current auditor from the X-User-Id header, defaulting to SYSTEM. */
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return Optional.of("SYSTEM");

            HttpServletRequest req = attrs.getRequest();
            String userId = req.getHeader("X-User-Id");
            return Optional.of(userId != null && !userId.isBlank() ? userId : "SYSTEM");
        };
    }
}