package com.cts.authservice.service;

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
import com.cts.authservice.service.impl.AuthServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthServiceImpl} covering registration, login by
 * username/email, and token validation including failure scenarios.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock AuthRepository authRepository;
    @Mock UserServiceGateway userServiceGateway;
    @Mock PasswordEncoder passwordEncoder; // see note below
    @Mock JwtUtil jwtUtil;
    @InjectMocks AuthServiceImpl service;

    private RegisterRequestDTO registerReq;
    private LoginRequestDTO loginReq;

    @BeforeEach
    void setUp() {
        registerReq = RegisterRequestDTO.builder()
                .name("Bob").username("bobby").email("bob@x.com")
                .password("secret").phoneNumber("1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1)).build();

        loginReq = LoginRequestDTO.builder()
                .usernameOrEmail("bob").password("secret").build();
    }

    @Test
    void register_success() {
        RegisterResponseDTO created = RegisterResponseDTO.builder()
                .userId(2L).username("bobby").email("bob@x.com").build();
        when(userServiceGateway.createUser(any())).thenReturn(created);
        when(passwordEncoder.encode("secret")).thenReturn("hash");
        when(authRepository.save(any(Auth.class))).thenReturn(mock(Auth.class));

        RegisterResponseDTO out = service.register(registerReq);

        assertThat(out.getUserId()).isEqualTo(2L);
        verify(authRepository).save(any(Auth.class));
    }

    @Test
    void login_byUsername_success() {
        UserDTO user = UserDTO.builder().userId(2L).role("CUSTOMER").build();
        when(userServiceGateway.getUserByUsername("bob")).thenReturn(user);
        Auth auth = Auth.builder().userId(2L).hashedPassword("hash").build();
        when(authRepository.findByUserId(2L)).thenReturn(Optional.of(auth));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(jwtUtil.generateToken(2L, "CUSTOMER")).thenReturn("tok");

        LoginResponseDTO out = service.login(loginReq);

        assertThat(out.getToken()).isEqualTo("Bearer tok");
    }

    @Test
    void login_byEmail_success() {
        loginReq.setUsernameOrEmail("bob@x.com");
        UserDTO user = UserDTO.builder().userId(2L).role("CUSTOMER").build();
        when(userServiceGateway.getUserByEmail("bob@x.com")).thenReturn(user);
        Auth auth = Auth.builder().userId(2L).hashedPassword("hash").build();
        when(authRepository.findByUserId(2L)).thenReturn(Optional.of(auth));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(jwtUtil.generateToken(2L, "CUSTOMER")).thenReturn("tok");

        assertThat(service.login(loginReq).getToken()).isEqualTo("Bearer tok");
        verify(userServiceGateway).getUserByEmail("bob@x.com");
    }

    @Test
    void login_noAuthRow_throws() {
        UserDTO user = UserDTO.builder().userId(2L).build();
        when(userServiceGateway.getUserByUsername("bob")).thenReturn(user);
        when(authRepository.findByUserId(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(loginReq))
                .isInstanceOf(AuthException.class)
                .hasMessage("Invalid Credentials");
    }

    @Test
    void login_wrongPassword_throws() {
        UserDTO user = UserDTO.builder().userId(2L).build();
        when(userServiceGateway.getUserByUsername("bob")).thenReturn(user);
        Auth auth = Auth.builder().userId(2L).hashedPassword("hash").build();
        when(authRepository.findByUserId(2L)).thenReturn(Optional.of(auth));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(loginReq))
                .isInstanceOf(AuthException.class)
                .hasMessage("Invalid Credentials");
    }

    @Test
    void validate_success() {
        when(jwtUtil.getUserIdFromToken("abc")).thenReturn(2L);
        when(jwtUtil.getRoleFromToken("abc")).thenReturn("CUSTOMER");

        ValidateResponseDTO out = service.validate("Bearer abc");

        assertThat(out.getUserId()).isEqualTo(2L);
        assertThat(out.getRole()).isEqualTo("CUSTOMER");
    }

    @Test
    void validate_nullHeader_throws() {
        assertThatThrownBy(() -> service.validate(null))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Missing or Malformed");
    }

    @Test
    void validate_malformedHeader_throws() {
        assertThatThrownBy(() -> service.validate("Token abc"))
                .isInstanceOf(AuthException.class);
    }
}