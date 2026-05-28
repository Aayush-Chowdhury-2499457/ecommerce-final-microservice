package com.cts.productservice.controller;

import com.cts.productservice.dto.CreateProductDTO;
import com.cts.productservice.dto.ProductResponseDTO;
import com.cts.productservice.dto.UpdateProductDTO;
import com.cts.productservice.dto.UpdateStockDTO;
import com.cts.productservice.service.ProductService;
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
    public ResponseEntity<ProductResponseDTO> create(@Valid @RequestBody CreateProductDTO dto) {
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
    public ResponseEntity<ProductResponseDTO> update(@PathVariable Long productId,
                                                    @Valid @RequestBody UpdateProductDTO dto) {
        return ResponseEntity.ok(productService.update(productId, dto));
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<ProductResponseDTO> updateStock(@PathVariable Long productId,
                                                         @Valid @RequestBody UpdateStockDTO dto) {
        return ResponseEntity.ok(productService.updateStock(productId, dto));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@PathVariable Long productId) {
        productService.delete(productId);
        return ResponseEntity.noContent().build();
    }
}
