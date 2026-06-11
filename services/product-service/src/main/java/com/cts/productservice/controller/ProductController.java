package com.cts.productservice.controller;

import com.cts.productservice.dto.*;
import com.cts.productservice.service.ProductService;
import com.cts.productservice.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for managing products and their stock levels.
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /** Creates a new product (ADMIN only). */
    @PostMapping
    public ResponseEntity<ProductResponseDTO> create(@RequestHeader(value = "X-User-Role", required = false) String role,
                                                     @Valid @RequestBody CreateProductDTO dto) {
        log.info("Create product request, role={}", role);
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(dto));
    }

    /** Returns all products. */
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> all() {
        log.debug("Fetching all products");
        return ResponseEntity.ok(productService.findAll());
    }

    /** Returns a single product by id. */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> byId(@PathVariable Long productId) {
        log.debug("Fetching product {}", productId);
        return ResponseEntity.ok(productService.findById(productId));
    }

    /** Searches products by (partial) name. */
    @GetMapping("/productName/{productName}")
    public ResponseEntity<List<ProductResponseDTO>> byName(@PathVariable String productName) {
        log.debug("Searching products by name {}", productName);
        return ResponseEntity.ok(productService.findByName(productName));
    }

    /** Returns all products belonging to a category. */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponseDTO>> byCategory(@PathVariable Long categoryId) {
        log.debug("Fetching products for category {}", categoryId);
        return ResponseEntity.ok(productService.findByCategory(categoryId));
    }

    /** Updates a product (ADMIN only). */
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> update(@RequestHeader(value = "X-User-Role", required = false) String role,
                                                     @PathVariable Long productId,
                                                     @Valid @RequestBody UpdateProductDTO dto) {
        log.info("Update product {} request, role={}", productId, role);
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(productService.update(productId, dto));
    }

    /** Sets the absolute stock level for a product (ADMIN only). */
    @PatchMapping("/{productId}/stock")
    public ResponseEntity<ProductResponseDTO> updateStock(@RequestHeader(value = "X-User-Role", required = false) String role,
                                                          @PathVariable Long productId,
                                                          @Valid @RequestBody StockQuantityDTO dto) {
        log.info("Update stock for product {} request, role={}", productId, role);
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(productService.updateStock(productId, dto));
    }

    /** Reduces stock; admin role enforced only when a role header is present (allows internal calls). */
    @PutMapping("/{productId}/reduce-stock")
    public ResponseEntity<ProductResponseDTO> reduceStock(@RequestHeader(value = "X-User-Role", required = false) String role,
                                                          @PathVariable Long productId, @Valid @RequestBody StockQuantityDTO dto) {
        log.info("Reduce stock for product {} request, role={}", productId, role);
        AuthUtil.requireRoleIfPresent(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(productService.reduceStock(productId, dto));
    }

    /** Deletes a product (ADMIN only). */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@RequestHeader(value = "X-User-Role", required = false) String role,
                                       @PathVariable Long productId) {
        log.info("Delete product {} request, role={}", productId, role);
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        productService.delete(productId);
        return ResponseEntity.noContent().build();
    }

    /** Increases stock; admin role enforced only when a role header is present (allows internal calls). */
    @PutMapping("/{productId}/restock")
    public ResponseEntity<ProductResponseDTO> restock(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long productId, @Valid @RequestBody StockQuantityDTO dto) {
        log.info("Restock product {} request, role={}", productId, role);
        AuthUtil.requireRoleIfPresent(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(productService.restock(productId, dto));
    }
}
