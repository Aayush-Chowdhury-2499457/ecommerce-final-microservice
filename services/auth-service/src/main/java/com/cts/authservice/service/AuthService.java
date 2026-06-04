package com.cts.authservice.service;

import com.cts.authservice.dto.request.LoginRequestDTO;
import com.cts.authservice.dto.request.RegisterRequestDTO;
import com.cts.authservice.dto.response.LoginResponseDTO;
import com.cts.authservice.dto.response.RegisterResponseDTO;
import com.cts.authservice.dto.response.ValidateResponseDTO;


/**
 * Defines authentication operations: registration, login, and token validation.
 */
public interface AuthService {
    /**
     * Registers a new user and persists their credentials.
     *
     * @param registerRequestDTO the registration payload
     * @return the created user details
     */
    RegisterResponseDTO register(RegisterRequestDTO registerRequestDTO);

    /**
     * Authenticates a user and issues a JWT token.
     *
     * @param loginRequestDTO the login credentials
     * @return the issued token
     */
    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);

    /**
     * Validates an Authorization header and resolves the user identity.
     *
     * @param authHeader the bearer Authorization header
     * @return the resolved user id and role
     */
    ValidateResponseDTO validate(String authHeader);
}
