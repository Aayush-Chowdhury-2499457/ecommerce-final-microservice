package com.cts.authservice.service.impl;

import com.cts.authservice.client.UserServiceClient;
import com.cts.authservice.dto.request.CreateUserRequestDTO;
import com.cts.authservice.dto.request.LoginRequestDTO;
import com.cts.authservice.dto.request.RegisterRequestDTO;
import com.cts.authservice.dto.response.LoginResponseDTO;
import com.cts.authservice.dto.response.RegisterResponseDTO;
import com.cts.authservice.dto.response.UserDTO;
import com.cts.authservice.dto.response.ValidateResponseDTO;
import com.cts.authservice.entity.Auth;
import com.cts.authservice.exception.custom.AuthException;
import com.cts.authservice.exception.custom.ServiceUnavailableException;
import com.cts.authservice.repository.AuthRepository;
import com.cts.authservice.security.util.JwtUtil;
import com.cts.authservice.service.AuthService;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String USER_SERVICE_CB = "userService";
    private final AuthRepository authRepository;
    private final UserServiceClient userServiceClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;



    @Override
    @Transactional
    public RegisterResponseDTO register(RegisterRequestDTO registerRequestDTO) {
        // 1. Build the create-user payload
        CreateUserRequestDTO createUserRequestDTO = CreateUserRequestDTO.builder()
                    .name(registerRequestDTO.getName())
                    .username(registerRequestDTO.getUsername())
                    .email(registerRequestDTO.getEmail())
                    .phoneNumber(registerRequestDTO.getPhoneNumber())
                    .dateOfBirth(registerRequestDTO.getDateOfBirth())
                    .build();

        // 2. Call user-service to create the user
        RegisterResponseDTO registerResponseDTO = createUserInUserService(createUserRequestDTO);

        // 3. Save credentials locally
        Auth auth = Auth.builder()
                .userId(registerResponseDTO.getUserId())
                .hashedPassword(passwordEncoder.encode(registerRequestDTO.getPassword()))
                .build();
        authRepository.save(auth);

        // 4. Return the Register Response
        return registerResponseDTO;
    }

    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        // 1. Find User in User Service by Username or Email
        UserDTO userDTO = resolveUser(loginRequestDTO.getUsernameOrEmail());

        // 2. Match userId to auth row
        Auth auth = authRepository.findByUserId(userDTO.getUserId())
                .orElseThrow(() -> new AuthException("Invalid Credentials"));

        // 3. Compare password
        if(!passwordEncoder.matches(loginRequestDTO.getPassword(), auth.getHashedPassword())) {
            throw new AuthException("Invalid Credentials");
        }

        // 4. Issue JWT
        String token = jwtUtil.generateToken(userDTO.getUserId(), userDTO.getRole());

        return new LoginResponseDTO("Bearer " + token);
    }

    private UserDTO resolveUser(String usernameOrEmail) {
        if(usernameOrEmail.contains("@")) {
            return getUserByEmail(usernameOrEmail);
        }
        return getUserByUsername(usernameOrEmail);
    }

    @Override
    @Transactional
    public ValidateResponseDTO validate(String authHeader) {
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthException("Missing or Malformed Authorization Header");
        }
        String token = authHeader.substring(7).trim();

        Long userId = jwtUtil.getUserIdFromToken(token);
        String role =  jwtUtil.getRoleFromToken(token);

        return ValidateResponseDTO.builder()
                .userId(userId)
                .role(role)
                .build();
    }

    // ---------- Feign Helpers with Circuit Breaker and Retry ---------- //

    @Retry(name = USER_SERVICE_CB)
    @CircuitBreaker(name = USER_SERVICE_CB, fallbackMethod = "createUserFallback")
    public RegisterResponseDTO createUserInUserService(CreateUserRequestDTO createUserRequestDTO) {
        ResponseEntity<RegisterResponseDTO> response = userServiceClient.createUser(createUserRequestDTO);
        return response.getBody();
    }

    public RegisterResponseDTO createUserFallback(CreateUserRequestDTO createUserRequestDTO, Throwable ex) {
        log.error("User Service Fallback for Registration: {}", ex.getMessage());
        throw new ServiceUnavailableException("User Service Unavailable, cannot create user");
    }

    @Retry(name = USER_SERVICE_CB)
    @CircuitBreaker(name = USER_SERVICE_CB, fallbackMethod = "userFetchFallback")
    public UserDTO getUserByUsername(String username) {
        ResponseEntity<UserDTO> response = userServiceClient.getUserByUsername(username);
        return response.getBody();
    }

    @Retry(name = USER_SERVICE_CB)
    @CircuitBreaker(name = USER_SERVICE_CB, fallbackMethod = "userFetchFallback")
    public UserDTO getUserByEmail(String email) {
        ResponseEntity<UserDTO> response =  userServiceClient.getUserByEmail(email);
        return response.getBody();
    }

    public UserDTO userFetchFallback(String username, Throwable ex) {
        if(ex instanceof FeignException fe && fe.status() == 404) {
            throw new AuthException("Invalid Credentials");
        }
        log.error("User Service Fallback for Login: {}", ex.getMessage());
        throw new ServiceUnavailableException("User Service Unavailable, cannot login");
    }
}
