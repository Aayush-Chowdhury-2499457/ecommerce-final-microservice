package com.cts.authservice.controller;

import com.cts.authservice.dto.request.LoginRequestDTO;
import com.cts.authservice.dto.request.RegisterRequestDTO;
import com.cts.authservice.dto.response.LoginResponseDTO;
import com.cts.authservice.dto.response.RegisterResponseDTO;
import com.cts.authservice.dto.response.ValidateResponseDTO;
import com.cts.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing authentication endpoints for registration,
 * login, and token validation.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user and returns the created user details.
     *
     * @param registerRequestDTO the registration payload
     * @return the created user wrapped in a 201 response
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        log.info("Received registration request for username: {}", registerRequestDTO.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerRequestDTO));
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param loginRequestDTO the login credentials
     * @return the issued token wrapped in a 200 response
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        log.info("Received login request for: {}", loginRequestDTO.getUsernameOrEmail());
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(loginRequestDTO));
    }

    /**
     * Validates the supplied Authorization header and resolves user identity.
     *
     * @param authHeader the bearer Authorization header
     * @return the resolved user id and role wrapped in a 200 response
     */
    @PostMapping("/validate")
    public ResponseEntity<ValidateResponseDTO> validate(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        log.debug("Received token validation request");
        return ResponseEntity.status(HttpStatus.OK).body(authService.validate(authHeader));
    }

}
