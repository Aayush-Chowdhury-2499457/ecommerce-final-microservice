package com.cts.authservice.service.impl;

import com.cts.authservice.dto.request.CreateUserRequestDTO;
import com.cts.authservice.dto.request.LoginRequestDTO;
import com.cts.authservice.dto.request.RegisterRequestDTO;
import com.cts.authservice.dto.response.LoginResponseDTO;
import com.cts.authservice.dto.response.RegisterResponseDTO;
import com.cts.authservice.dto.response.UserDTO;
import com.cts.authservice.dto.response.ValidateResponseDTO;
import com.cts.authservice.entity.Auth;
import com.cts.authservice.exception.custom.AuthException;
import com.cts.authservice.gateway.UserServiceGateway;
import com.cts.authservice.repository.AuthRepository;
import com.cts.authservice.security.util.JwtUtil;
import com.cts.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final UserServiceGateway userServiceGateway;
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
        RegisterResponseDTO registerResponseDTO = userServiceGateway.createUser(createUserRequestDTO);

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
        if (usernameOrEmail.contains("@")) {
            return userServiceGateway.getUserByEmail(usernameOrEmail);
        }
        return userServiceGateway.getUserByUsername(usernameOrEmail);
    }

    @Override
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
}
