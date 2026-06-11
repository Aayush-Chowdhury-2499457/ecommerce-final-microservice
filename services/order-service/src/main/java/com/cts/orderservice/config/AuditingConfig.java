package com.cts.orderservice.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * JPA auditing configuration. Supplies the current auditor (the caller's userId,
 * read from the gateway-injected X-User-Id header) for {@code @CreatedBy}/{@code @LastModifiedBy}.
 */
@Slf4j
@Configuration
public class AuditingConfig {

    /**
     * Pulls the caller's userId from the X-User-Id header injected by the API Gateway
     * (the gateway validates the JWT and forwards the userId as a header).
     * Falls back to "SYSTEM" if the header isn't present (e.g. internal calls).
     */
    /**
     * Provides the current auditor for JPA auditing.
     *
     * @return an {@link AuditorAware} resolving the caller's userId, or "SYSTEM" when absent
     */
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
