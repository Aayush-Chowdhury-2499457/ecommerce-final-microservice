package com.cts.productservice.controller;

import com.cts.productservice.dto.*;
import com.cts.productservice.service.ProductService;
import com.cts.productservice.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponseDTO> create(@RequestHeader(value = "X-User-Role", required = false) String role,
                                                     @Valid @RequestBody CreateProductDTO dto) {
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> all() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> byId(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.findById(productId));
    }

    @GetMapping("/productName/{productName}")
    public ResponseEntity<List<ProductResponseDTO>> byName(@PathVariable String productName) {
        return ResponseEntity.ok(productService.findByName(productName));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponseDTO>> byCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.findByCategory(categoryId));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> update(@RequestHeader(value = "X-User-Role", required = false) String role,
                                                     @PathVariable Long productId,
                                                     @Valid @RequestBody UpdateProductDTO dto) {
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(productService.update(productId, dto));
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<ProductResponseDTO> updateStock(@RequestHeader(value = "X-User-Role", required = false) String role,
                                                          @PathVariable Long productId,
                                                          @Valid @RequestBody UpdateStockDTO dto) {
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(productService.updateStock(productId, dto));
    }

    @PatchMapping("/{productId}/reduce-stock")
    public ResponseEntity<ProductResponseDTO> reduceStock(@RequestHeader(value = "X-User-Role", required = false) String role,
                                                          @PathVariable Long productId, @Valid @RequestBody ReduceStockDTO dto) {
        AuthUtil.requireRoleIfPresent(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(productService.reduceStock(productId, dto));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@RequestHeader(value = "X-User-Role", required = false) String role,
                                       @PathVariable Long productId) {
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        productService.delete(productId);
        return ResponseEntity.noContent().build();
    }
}
