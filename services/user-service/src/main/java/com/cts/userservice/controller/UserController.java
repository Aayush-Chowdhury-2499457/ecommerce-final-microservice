package com.cts.userservice.controller;

import com.cts.userservice.dto.CreateUserDTO;
import com.cts.userservice.dto.UpdateUserDTO;
import com.cts.userservice.dto.UserResponseDTO;
import com.cts.userservice.service.UserService;
import com.cts.userservice.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing user CRUD endpoints under {@code /api/users}.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** Registers a new user and returns it with HTTP 201. */
    @PostMapping
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody CreateUserDTO dto) {
        log.info("Received request to create user {}", dto.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(dto));
    }

    /** Lists all users; restricted to admins. */
    @GetMapping  // ADMIN
    public ResponseEntity<List<UserResponseDTO>> all(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        log.debug("Received request to list all users");
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(userService.findAll());
    }

    /** Returns a user by id; allowed for the owner or an admin. */
    @GetMapping("/{userId}")  // self-or-admin
    public ResponseEntity<UserResponseDTO> byId(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        log.debug("Received request to fetch user by id {}", userId);
        AuthUtil.requireSelfOrAdmin(userId, callerId, role);
        return ResponseEntity.ok(userService.findById(userId));
    }

    /** Returns a user by username; admin-only when a role header is present. */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponseDTO> byUsername(@PathVariable String username,
                                                      @RequestHeader(value = "X-User-Role", required = false) String role) {
        log.debug("Received request to fetch user by username {}", username);
        AuthUtil.requireRoleIfPresent(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(userService.findByUsername(username));
    }

    /** Returns a user by email; admin-only when a role header is present. */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> byEmail(@PathVariable String email,
                                                   @RequestHeader(value = "X-User-Role", required = false) String role) {
        log.debug("Received request to fetch user by email {}", email);
        AuthUtil.requireRoleIfPresent(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(userService.findByEmail(email));
    }

    /** Updates a user; allowed only for the resource owner. */
    @PutMapping("/{userId}")  // self-or-admin
    public ResponseEntity<UserResponseDTO> update(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @Valid @RequestBody UpdateUserDTO dto) {
        log.info("Received request to update user {}", userId);
        AuthUtil.requireOwner(userId, callerId);
        return ResponseEntity.ok(userService.update(userId, dto));
    }

    /** Deletes a user; restricted to admins. */
    @DeleteMapping("/{userId}")  // ADMIN
    public ResponseEntity<Void> delete(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long userId) {
        log.info("Received request to delete user {}", userId);
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}