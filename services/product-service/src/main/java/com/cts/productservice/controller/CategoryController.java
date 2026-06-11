package com.cts.productservice.controller;

import com.cts.productservice.dto.CategoryDTO;
import com.cts.productservice.dto.CategoryResponseDTO;
import com.cts.productservice.service.CategoryService;
import com.cts.productservice.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for managing product categories.
 */
@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /** Creates a new category (ADMIN only). */
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> create(@RequestHeader(value = "X-User-Role", required = false) String role,
                                                      @Valid @RequestBody CategoryDTO dto) {
        log.info("Create category request, role={}", role);
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(dto));
    }

    /** Returns all categories. */
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> all() {
        log.debug("Fetching all categories");
        return ResponseEntity.ok(categoryService.findAll());
    }

    /** Returns a single category by id. */
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> byId(@PathVariable Long categoryId) {
        log.debug("Fetching category {}", categoryId);
        return ResponseEntity.ok(categoryService.findById(categoryId));
    }

    /** Returns a category by its exact name. */
    @GetMapping("/categoryName/{categoryName}")
    public ResponseEntity<CategoryResponseDTO> byName(@PathVariable String categoryName) {
        log.debug("Fetching category by name {}", categoryName);
        return ResponseEntity.ok(categoryService.findByName(categoryName));
    }

    /** Updates a category (ADMIN only). */
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> update(@RequestHeader(value = "X-User-Role", required = false) String role,
                                                      @PathVariable Long categoryId,
                                                      @Valid @RequestBody CategoryDTO dto) {
        log.info("Update category {} request, role={}", categoryId, role);
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(categoryService.update(categoryId, dto));
    }

    /** Deletes a category (ADMIN only). */
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> delete(@RequestHeader(value = "X-User-Role", required = false) String role,
                                       @PathVariable Long categoryId) {
        log.info("Delete category {} request, role={}", categoryId, role);
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        categoryService.delete(categoryId);
        return ResponseEntity.noContent().build();
    }
}
