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

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceGateway {

    private static final String USER_SERVICE_CB = "userService";
    private final UserServiceClient userServiceClient;

    @RateLimiter(name = USER_SERVICE_CB)
    @Retry(name = USER_SERVICE_CB)
    @CircuitBreaker(name = USER_SERVICE_CB, fallbackMethod = "createUserFallback")
    public RegisterResponseDTO createUser(CreateUserRequestDTO dto) {
        ResponseEntity<RegisterResponseDTO> response = userServiceClient.createUser(dto);
        return response.getBody();
    }

    public RegisterResponseDTO createUserFallback(CreateUserRequestDTO dto, Throwable ex) {
        log.error("User Service Fallback for Registration: {}", ex.getMessage());
        throw new ServiceUnavailableException("User Service Unavailable, cannot create user");
    }

    @RateLimiter(name = USER_SERVICE_CB)
    @Retry(name = USER_SERVICE_CB)
    @CircuitBreaker(name = USER_SERVICE_CB, fallbackMethod = "userFetchFallback")
    public UserDTO getUserByUsername(String username) {
        ResponseEntity<UserDTO> response = userServiceClient.getUserByUsername(username);
        return response.getBody();
    }

    @Retry(name = USER_SERVICE_CB)
    @CircuitBreaker(name = USER_SERVICE_CB, fallbackMethod = "userFetchFallback")
    public UserDTO getUserByEmail(String email) {
        ResponseEntity<UserDTO> response = userServiceClient.getUserByEmail(email);
        return response.getBody();
    }

    // Runs for BOTH username/email lookups; CustomErrorDecoder wraps 404 as DownstreamException, not FeignException
    public UserDTO userFetchFallback(String key, Throwable ex) {
        if (ex instanceof DownstreamException de && de.getStatusCode() == 404) {
            throw new AuthException("Invalid Credentials");
        }
        log.error("User Service Fallback for Login: {}", ex.getMessage());
        throw new ServiceUnavailableException("User Service Unavailable, please try again later");
    }
}