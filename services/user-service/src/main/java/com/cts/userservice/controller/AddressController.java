package com.cts.userservice.controller;

import com.cts.userservice.dto.AddressDTO;
import com.cts.userservice.dto.AddressResponseDTO;
import com.cts.userservice.service.AddressService;
import com.cts.userservice.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing per-user address endpoints under
 * {@code /api/users/{userId}/addresses}.
 */
@Slf4j
@RestController
@RequestMapping("/api/users/{userId}/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /** Adds an address for a user; allowed for the owner or an admin. */
    @PostMapping  // self-or-admin
    public ResponseEntity<AddressResponseDTO> add(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody AddressDTO dto) {
        log.info("Received request to add address for user {}", userId);
        AuthUtil.requireSelfOrAdmin(userId, callerId, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.add(userId, dto));
    }

    /** Lists a user's addresses; allowed for the owner or an admin. */
    @GetMapping  // self-or-admin
    public ResponseEntity<List<AddressResponseDTO>> list(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        log.debug("Received request to list addresses for user {}", userId);
        AuthUtil.requireSelfOrAdmin(userId, callerId, role);
        return ResponseEntity.ok(addressService.listForUser(userId));
    }

    /** Returns a single address; authorization is skipped for internal calls without a caller id. */
    @GetMapping("/{addressId}")  // also called internally by order-service -> skip when no caller
    public ResponseEntity<AddressResponseDTO> getOne(
            @PathVariable Long userId, @PathVariable Long addressId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        log.debug("Received request to fetch address {} for user {}", addressId, userId);
        if (callerId != null) AuthUtil.requireSelfOrAdmin(userId, callerId, role);
        return ResponseEntity.ok(addressService.getOne(userId, addressId));
    }


    /** Updates an address; allowed for the owner or an admin. */
    @PutMapping("/{addressId}")  // self-or-admin
    public ResponseEntity<AddressResponseDTO> update(
            @PathVariable Long userId, @PathVariable Long addressId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody AddressDTO dto) {
        log.info("Received request to update address {} for user {}", addressId, userId);
        AuthUtil.requireSelfOrAdmin(userId, callerId, role);
        return ResponseEntity.ok(addressService.update(userId, addressId, dto));
    }


    /** Deletes an address; allowed for the owner or an admin. */
    @DeleteMapping("/{addressId}")  // self-or-admin
    public ResponseEntity<Void> delete(
            @PathVariable Long userId, @PathVariable Long addressId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        log.info("Received request to delete address {} for user {}", addressId, userId);
        AuthUtil.requireSelfOrAdmin(userId, callerId, role);
        addressService.delete(userId, addressId);
        return ResponseEntity.noContent().build();
    }
}