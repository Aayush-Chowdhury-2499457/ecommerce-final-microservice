package com.cts.userservice.controller;

import com.cts.userservice.dto.AddressDTO;
import com.cts.userservice.dto.AddressResponseDTO;
import com.cts.userservice.service.AddressService;
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

    @PostMapping
    public ResponseEntity<AddressResponseDTO> add(@PathVariable Long userId,
                                                  @Valid @RequestBody AddressDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.add(userId, dto));
    }

    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> list(@PathVariable Long userId) {
        return ResponseEntity.ok(addressService.listForUser(userId));
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponseDTO> getOne(@PathVariable Long userId,
                                                     @PathVariable Long addressId) {
        return ResponseEntity.ok(addressService.getOne(userId, addressId));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponseDTO> update(@PathVariable Long userId,
                                                     @PathVariable Long addressId,
                                                     @Valid @RequestBody AddressDTO dto) {
        return ResponseEntity.ok(addressService.update(userId, addressId, dto));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId,
                                       @PathVariable Long addressId) {
        addressService.delete(userId, addressId);
        return ResponseEntity.noContent().build();
    }
}