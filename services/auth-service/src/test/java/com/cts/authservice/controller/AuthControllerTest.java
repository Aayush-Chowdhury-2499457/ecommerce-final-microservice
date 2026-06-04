package com.cts.authservice.controller;

import com.cts.authservice.dto.request.LoginRequestDTO;
import com.cts.authservice.dto.request.RegisterRequestDTO;
import com.cts.authservice.dto.response.LoginResponseDTO;
import com.cts.authservice.dto.response.RegisterResponseDTO;
import com.cts.authservice.dto.response.ValidateResponseDTO;
import com.cts.authservice.exception.GlobalExceptionHandler;
import com.cts.authservice.exception.custom.AuthException;
import com.cts.authservice.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Standalone MockMvc tests for {@link AuthController} covering the
 * register, login, and validate endpoints and their error paths.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock AuthService authService;
    @InjectMocks AuthController controller;

    MockMvc mvc;
    final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private RegisterRequestDTO validRegister;
    private LoginRequestDTO validLogin;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        validRegister = RegisterRequestDTO.builder()
                .name("Bob").username("bobby").email("bob@x.com")
                .password("secret").phoneNumber("1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1)).build();

        validLogin = LoginRequestDTO.builder()
                .usernameOrEmail("bob").password("secret").build();
    }

    @Test
    void register_returns201() throws Exception {
        RegisterResponseDTO created = RegisterResponseDTO.builder()
                .userId(2L).username("bobby").email("bob@x.com").build();
        when(authService.register(any())).thenReturn(created);

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validRegister)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.username").value("bobby"));
    }

    @Test
    void register_invalidBody_returns400() throws Exception {
        validRegister.setUsername("");          // @NotBlank/@Size
        validRegister.setEmail("not-an-email"); // @Email
        validRegister.setPassword("123");       // @Size(min=6)
        validRegister.setPhoneNumber("123");    // @Pattern

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validRegister)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_returns200() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponseDTO("Bearer tok"));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("Bearer tok"));
    }

    @Test
    void login_badCredentials_returns401() throws Exception {
        when(authService.login(any())).thenThrow(new AuthException("Invalid Credentials"));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid Credentials"));
    }

    @Test
    void validate_returns200() throws Exception {
        when(authService.validate("Bearer abc"))
                .thenReturn(ValidateResponseDTO.builder().userId(2L).role("CUSTOMER").build());

        mvc.perform(post("/api/auth/validate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void validate_malformedHeader_returns401() throws Exception {
        when(authService.validate(any()))
                .thenThrow(new AuthException("Missing or Malformed Authorization Header"));

        mvc.perform(post("/api/auth/validate")
                        .header(HttpHeaders.AUTHORIZATION, "Token abc"))
                .andExpect(status().isUnauthorized());
    }
}