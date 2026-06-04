package com.cts.authservice.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility for generating and parsing JWT tokens used for authentication.
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Getter
    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey secretKey;

    /**
     * Initializes the signing key from the configured secret after construction.
     */
    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.debug("JWT signing key initialized");
    }

    /**
     * Generates a signed JWT for the given user id and role.
     *
     * @param userId the user id to embed as subject/claim
     * @param role   the user role claim
     * @return a compact signed JWT
     */
    public String generateToken(Long userId, String role) {
        log.debug("Generating JWT for userId: {}", userId);
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Parses and verifies a JWT, returning its claims.
     *
     * @param token the compact JWT
     * @return the verified token claims
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the user id claim from a JWT.
     *
     * @param token the compact JWT
     * @return the user id
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parse(token);
        return claims.get("userId", Number.class).longValue();
    }

    /**
     * Extracts the role claim from a JWT.
     *
     * @param token the compact JWT
     * @return the user role
     */
    public String getRoleFromToken(String token) {
        Claims claims = parse(token);
        return claims.get("role").toString();
    }

}
