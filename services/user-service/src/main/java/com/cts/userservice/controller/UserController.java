package com.cts.userservice.controller;

import com.cts.userservice.dto.CreateUserDTO;
import com.cts.userservice.dto.UpdateUserDTO;
import com.cts.userservice.dto.UserResponseDTO;
import com.cts.userservice.service.UserService;
import com.cts.userservice.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody CreateUserDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(dto));
    }

    @GetMapping  // ADMIN
    public ResponseEntity<List<UserResponseDTO>> all(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{userId}")  // self-or-admin
    public ResponseEntity<UserResponseDTO> byId(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        AuthUtil.requireSelfOrAdmin(userId, callerId, role);
        return ResponseEntity.ok(userService.findById(userId));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponseDTO> byUsername(@PathVariable String username,
                                                      @RequestHeader(value = "X-User-Role", required = false) String role) {
        AuthUtil.requireRoleIfPresent(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(userService.findByUsername(username));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> byEmail(@PathVariable String email,
                                                   @RequestHeader(value = "X-User-Role", required = false) String role) {
        AuthUtil.requireRoleIfPresent(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(userService.findByEmail(email));
    }

    @PutMapping("/{userId}")  // self-or-admin
    public ResponseEntity<UserResponseDTO> update(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @Valid @RequestBody UpdateUserDTO dto) {
        AuthUtil.requireOwner(userId, callerId);
        return ResponseEntity.ok(userService.update(userId, dto));
    }

    @DeleteMapping("/{userId}")  // ADMIN
    public ResponseEntity<Void> delete(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long userId) {
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}