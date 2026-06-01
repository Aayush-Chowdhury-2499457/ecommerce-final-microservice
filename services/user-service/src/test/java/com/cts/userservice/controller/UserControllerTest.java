package com.cts.userservice.controller;

import com.cts.userservice.dto.CreateUserDTO;
import com.cts.userservice.dto.UpdateUserDTO;
import com.cts.userservice.dto.UserResponseDTO;
import com.cts.userservice.entity.Role;
import com.cts.userservice.exception.GlobalExceptionHandler;
import com.cts.userservice.exception.custom.DuplicateResourceException;
import com.cts.userservice.exception.custom.ResourceNotFoundException;
import com.cts.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock UserService userService;
    @InjectMocks UserController controller;

    MockMvc mvc;
    final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private UserResponseDTO resp;
    private CreateUserDTO validCreate;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        resp = UserResponseDTO.builder()
                .userId(1L).name("Bob").username("bob").email("bob@x.com")
                .phoneNumber("1234567890").role(Role.CUSTOMER).build();

        validCreate = new CreateUserDTO();
        validCreate.setName("Bob");
        validCreate.setUsername("bob");
        validCreate.setEmail("bob@x.com");
        validCreate.setPhoneNumber("1234567890");
        validCreate.setDateOfBirth(LocalDate.of(1990, 1, 1));
    }

    @Test
    void create_returns201() throws Exception {
        when(userService.create(any())).thenReturn(resp);

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("bob"));
    }

    @Test
    void create_invalidBody_returns400() throws Exception {
        validCreate.setUsername("");
        validCreate.setEmail("not-an-email");
        validCreate.setPhoneNumber("abc");

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validCreate)))
                .andExpect(status().isBadRequest());
        verify(userService, never()).create(any());
    }

    @Test
    void create_duplicate_returns409() throws Exception {
        when(userService.create(any()))
                .thenThrow(new DuplicateResourceException("Username already taken"));

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validCreate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already taken"));
    }

    @Test
    void all_asAdmin_returns200() throws Exception {
        when(userService.findAll()).thenReturn(List.of(resp));

        mvc.perform(get("/api/users").header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("bob"));
    }

    @Test
    void all_asNonAdmin_returns403() throws Exception {
        mvc.perform(get("/api/users").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
        verify(userService, never()).findAll();
    }

    @Test
    void byId_self_returns200() throws Exception {
        when(userService.findById(1L)).thenReturn(resp);

        mvc.perform(get("/api/users/1")
                        .header("X-User-Id", "1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void byId_otherUser_returns403() throws Exception {
        mvc.perform(get("/api/users/2")
                        .header("X-User-Id", "1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void byId_notFound_returns404() throws Exception {
        when(userService.findById(1L)).thenThrow(new ResourceNotFoundException("User not found: 1"));

        mvc.perform(get("/api/users/1")
                        .header("X-User-Id", "1").header("X-User-Role", "ADMIN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void byUsername_asAdmin_returns200() throws Exception {
        when(userService.findByUsername("bob")).thenReturn(resp);

        mvc.perform(get("/api/users/username/bob").header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void byUsername_noRole_internalCall_returns200() throws Exception {
        when(userService.findByUsername("bob")).thenReturn(resp);

        mvc.perform(get("/api/users/username/bob"))
                .andExpect(status().isOk());
    }

    @Test
    void byUsername_asCustomer_returns403() throws Exception {
        mvc.perform(get("/api/users/username/bob").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void byEmail_asAdmin_returns200() throws Exception {
        when(userService.findByEmail("bob@x.com")).thenReturn(resp);

        mvc.perform(get("/api/users/email/bob@x.com").header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void update_owner_returns200() throws Exception {
        when(userService.update(eq(1L), any())).thenReturn(resp);

        mvc.perform(put("/api/users/1").header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UpdateUserDTO())))
                .andExpect(status().isOk());
    }

    @Test
    void update_notOwner_returns403() throws Exception {
        mvc.perform(put("/api/users/1").header("X-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UpdateUserDTO())))
                .andExpect(status().isForbidden());
        verify(userService, never()).update(any(), any());
    }

    @Test
    void delete_asAdmin_returns204() throws Exception {
        mvc.perform(delete("/api/users/1").header("X-User-Role", "ADMIN"))
                .andExpect(status().isNoContent());
        verify(userService).delete(1L);
    }

    @Test
    void delete_asNonAdmin_returns403() throws Exception {
        mvc.perform(delete("/api/users/1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
        verify(userService, never()).delete(any());
    }

    @Test
    void unexpectedError_returns500() throws Exception {
        when(userService.findAll()).thenThrow(new RuntimeException("boom"));

        mvc.perform(get("/api/users").header("X-User-Role", "ADMIN"))
                .andExpect(status().isInternalServerError());
    }
}