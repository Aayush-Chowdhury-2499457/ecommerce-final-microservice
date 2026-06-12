package com.cts.productservice.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Configuration that supplies the auditor used to populate the {@code createdBy}
 * and {@code updatedBy} fields on audited entities.
 * <p>
 * The {@link AuditorAware} bean defined here is wired into JPA auditing via the
 * {@code auditorAwareRef} declared on
 * {@link com.cts.productservice.ProductServiceApplication}.
 *
 * @since 1.0
 */
@Configuration
public class AuditingConfig {

    /**
     * Provides the current auditor for Spring Data JPA auditing.
     * <p>
     * Resolves the caller's user id from the {@code X-User-Id} header injected by the
     * API Gateway (the gateway validates the JWT and forwards the id as a header).
     * Falls back to {@code "SYSTEM"} when no request context or header is available,
     * such as for internal or background calls.
     *
     * @return an {@link AuditorAware} that yields the current user id, or {@code "SYSTEM"}
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
