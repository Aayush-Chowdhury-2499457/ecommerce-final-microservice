package com.cts.authservice.client;

import com.cts.authservice.config.FeignConfig;
import com.cts.authservice.dto.request.CreateUserRequestDTO;
import com.cts.authservice.dto.response.RegisterResponseDTO;
import com.cts.authservice.dto.response.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client interface for communicating with the User Service.
 * This client provides methods to create a new user and retrieve user details
 * by username or email. It uses {@link FeignConfig} for configuration and
 * communicates with the service registered as "user-service".
 */
@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface UserServiceClient {

    /**
     * Creates a new user in the User Service.
     *
     * @param registerRequestDTO the request payload containing user details
     * @return a {@link ResponseEntity} containing the {@link RegisterResponseDTO}
     */
    @PostMapping("/api/users")
    ResponseEntity<RegisterResponseDTO> createUser(@RequestBody CreateUserRequestDTO registerRequestDTO);

    /**
     * Retrieves a user by their username.
     *
     * @param username the username of the user
     * @return a {@link ResponseEntity} containing the {@link UserDTO}
     */
    @GetMapping("/api/users/username/{username}")
    ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username);

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email address of the user
     * @return a {@link ResponseEntity} containing the {@link UserDTO}
     */
    @GetMapping("/api/users/email/{email}")
    ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email);

}
