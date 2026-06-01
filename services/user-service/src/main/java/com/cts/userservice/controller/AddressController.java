package com.cts.userservice.controller;

import com.cts.userservice.dto.AddressDTO;
import com.cts.userservice.dto.AddressResponseDTO;
import com.cts.userservice.service.AddressService;
import com.cts.userservice.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping  // self-or-admin
    public ResponseEntity<AddressResponseDTO> add(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody AddressDTO dto) {
        AuthUtil.requireSelfOrAdmin(userId, callerId, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.add(userId, dto));
    }

    @GetMapping  // self-or-admin
    public ResponseEntity<List<AddressResponseDTO>> list(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        AuthUtil.requireSelfOrAdmin(userId, callerId, role);
        return ResponseEntity.ok(addressService.listForUser(userId));
    }

    @GetMapping("/{addressId}")  // also called internally by order-service -> skip when no caller
    public ResponseEntity<AddressResponseDTO> getOne(
            @PathVariable Long userId, @PathVariable Long addressId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (callerId != null) AuthUtil.requireSelfOrAdmin(userId, callerId, role);
        return ResponseEntity.ok(addressService.getOne(userId, addressId));
    }


    @PutMapping("/{addressId}")  // self-or-admin
    public ResponseEntity<AddressResponseDTO> update(
            @PathVariable Long userId, @PathVariable Long addressId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody AddressDTO dto) {
        AuthUtil.requireSelfOrAdmin(userId, callerId, role);
        return ResponseEntity.ok(addressService.update(userId, addressId, dto));
    }


    @DeleteMapping("/{addressId}")  // self-or-admin
    public ResponseEntity<Void> delete(
            @PathVariable Long userId, @PathVariable Long addressId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        AuthUtil.requireSelfOrAdmin(userId, callerId, role);
        addressService.delete(userId, addressId);
        return ResponseEntity.noContent().build();
    }
}