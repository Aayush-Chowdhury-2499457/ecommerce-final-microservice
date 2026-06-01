package com.cts.authservice.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
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
