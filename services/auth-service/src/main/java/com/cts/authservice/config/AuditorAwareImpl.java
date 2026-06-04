package com.cts.authservice.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Configuration class that provides an {@link AuditorAware} implementation
 * for Spring Data JPA auditing.
 * <p>
 * This implementation retrieves the current auditor (userId) from the
 * {@code X-User-Id} HTTP header injected by the API Gateway after JWT validation.
 * If the header is not present, it falls back to {@code "SYSTEM"}.
 * </p>
 */
@Configuration
@Slf4j
public class AuditorAwareImpl {

    /**
     * Provides an {@link AuditorAware} bean that resolves the current auditor.
     * <p>
     * The auditor is determined by reading the {@code X-User-Id} header from the
     * current HTTP request. If no request is available or the header is missing,
     * the auditor defaults to {@code "SYSTEM"}.
     * </p>
     *
     * @return an {@link AuditorAware} instance that supplies the current auditor
     */
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attrs == null) {
                log.warn("No request context found. Falling back to SYSTEM auditor.");
                return Optional.of("SYSTEM");
            }

            HttpServletRequest req = attrs.getRequest();
            String userId = req.getHeader("X-User-Id");

            if (userId != null && !userId.isBlank()) {
                log.debug("Resolved auditor from X-User-Id header: {}", userId);
                return Optional.of(userId);
            } else {
                log.info("X-User-Id header missing or blank. Falling back to SYSTEM auditor.");
                return Optional.of("SYSTEM");
            }
        };
    }
}
