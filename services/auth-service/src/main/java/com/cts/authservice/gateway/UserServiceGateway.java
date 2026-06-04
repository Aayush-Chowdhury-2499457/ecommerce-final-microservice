package com.cts.authservice.gateway;

import com.cts.authservice.client.UserServiceClient;
import com.cts.authservice.dto.request.CreateUserRequestDTO;
import com.cts.authservice.dto.response.RegisterResponseDTO;
import com.cts.authservice.dto.response.UserDTO;
import com.cts.authservice.exception.custom.AuthException;
import com.cts.authservice.exception.custom.DownstreamException;
import com.cts.authservice.exception.custom.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Resilience-wrapped gateway around {@link UserServiceClient}, applying
 * rate limiting, retry, and circuit-breaker behavior with fallbacks.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceGateway {

    private static final String USER_SERVICE_CB = "userService";
    private final UserServiceClient userServiceClient;

    /**
     * Creates a user via the User Service, guarded by resilience policies.
     *
     * @param dto the create-user payload
     * @return the created user details
     */
    @RateLimiter(name = USER_SERVICE_CB)
    @Retry(name = USER_SERVICE_CB)
    @CircuitBreaker(name = USER_SERVICE_CB, fallbackMethod = "createUserFallback")
    public RegisterResponseDTO createUser(CreateUserRequestDTO dto) {
        log.debug("Calling user-service to create user: {}", dto.getUsername());
        ResponseEntity<RegisterResponseDTO> response = userServiceClient.createUser(dto);
        return response.getBody();
    }

    /**
     * Fallback for {@link #createUser} when the User Service is unreachable.
     *
     * @param dto the create-user payload
     * @param ex  the triggering throwable
     * @return never returns normally; always throws
     */
    public RegisterResponseDTO createUserFallback(CreateUserRequestDTO dto, Throwable ex) {
        log.error("User Service Fallback for Registration: {}", ex.getMessage());
        throw new ServiceUnavailableException("User Service Unavailable, cannot create user");
    }

    /**
     * Fetches a user by username, guarded by resilience policies.
     *
     * @param username the username to look up
     * @return the matching user details
     */
    @RateLimiter(name = USER_SERVICE_CB)
    @Retry(name = USER_SERVICE_CB)
    @CircuitBreaker(name = USER_SERVICE_CB, fallbackMethod = "userFetchFallback")
    public UserDTO getUserByUsername(String username) {
        log.debug("Calling user-service to fetch user by username: {}", username);
        ResponseEntity<UserDTO> response = userServiceClient.getUserByUsername(username);
        return response.getBody();
    }

    /**
     * Fetches a user by email, guarded by resilience policies.
     *
     * @param email the email to look up
     * @return the matching user details
     */
    @Retry(name = USER_SERVICE_CB)
    @CircuitBreaker(name = USER_SERVICE_CB, fallbackMethod = "userFetchFallback")
    public UserDTO getUserByEmail(String email) {
        log.debug("Calling user-service to fetch user by email: {}", email);
        ResponseEntity<UserDTO> response = userServiceClient.getUserByEmail(email);
        return response.getBody();
    }

    /**
     * Fallback for the user-lookup methods; maps a 404 to invalid credentials
     * and otherwise reports the service as unavailable.
     *
     * @param key the username or email that was looked up
     * @param ex  the triggering throwable
     * @return never returns normally; always throws
     */
    // Runs for BOTH username/email lookups; CustomErrorDecoder wraps 404 as DownstreamException, not FeignException
    public UserDTO userFetchFallback(String key, Throwable ex) {
        if (ex instanceof DownstreamException de && de.getStatusCode() == 404) {
            throw new AuthException("Invalid Credentials");
        }
        log.error("User Service Fallback for Login: {}", ex.getMessage());
        throw new ServiceUnavailableException("User Service Unavailable, please try again later");
    }
}