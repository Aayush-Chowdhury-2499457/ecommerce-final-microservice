package com.cts.authservice.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration defining the stateless filter chain,
 * public endpoints, and the password encoder.
 */
@Slf4j
@Configuration
public class SecurityConfig {

    private static final SessionCreationPolicy SESSION_POLICY = SessionCreationPolicy.STATELESS;
    private static final String[] PERMIT_ALL_MATCHERS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/validate" ,
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/webjars/**"
    };

    /**
     * Provides the BCrypt password encoder bean.
     *
     * @return a {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Builds the stateless security filter chain permitting public auth and docs endpoints.
     *
     * @param http the {@link HttpSecurity} builder
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the chain cannot be built
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.debug("Configuring stateless security filter chain");
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SESSION_POLICY))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PERMIT_ALL_MATCHERS).permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();

    }

}
