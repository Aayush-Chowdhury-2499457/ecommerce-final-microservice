package com.cts.productservice.controller;

import com.cts.productservice.dto.request.CategoryDTO;
import com.cts.productservice.dto.response.CategoryResponseDTO;
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
 * REST controller exposing product-category operations under {@code /api/categories}.
 * <p>
 * Read operations are open to any caller, whereas mutating operations are restricted
 * to administrators, enforced through {@link AuthUtil} against the {@code X-User-Role}
 * header forwarded by the API Gateway. Business logic is delegated to
 * {@link CategoryService}.
 *
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    /** Service that performs the category business operations. */
    private final CategoryService categoryService;

    /**
     * Creates a new category. Only administrators may perform this operation.
     *
     * @param role the caller's role from the {@code X-User-Role} header; must be {@code ADMIN}
     * @param dto  the {@link CategoryDTO} describing the category to create
     * @return {@code 201 CREATED} with the persisted {@link CategoryResponseDTO}
     * @throws com.cts.productservice.exception.custom.UnauthorizedAccessException if the caller is not an admin
     * @throws com.cts.productservice.exception.custom.DuplicateResourceException if a category with the same name already exists
     */
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> create(@RequestHeader(value = "X-User-Role", required = false) String role,
                                                      @Valid @RequestBody CategoryDTO dto) {
        log.info("POST create category name={}", dto.getCategoryName());
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(dto));
    }

    /**
     * Returns every category.
     *
     * @return {@code 200 OK} with the list of all {@link CategoryResponseDTO}s
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> all() {
        log.info("GET all categories");
        return ResponseEntity.ok(categoryService.findAll());
    }

    /**
     * Returns a single category by its identifier.
     *
     * @param categoryId the identifier of the category to fetch
     * @return {@code 200 OK} with the matching {@link CategoryResponseDTO}
     * @throws com.cts.productservice.exception.custom.ResourceNotFoundException if no category exists with the given id
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> byId(@PathVariable Long categoryId) {
        log.info("GET category by categoryId={}", categoryId);
        return ResponseEntity.ok(categoryService.findById(categoryId));
    }

    /**
     * Returns a single category by its exact name.
     *
     * @param categoryName the name of the category to fetch
     * @return {@code 200 OK} with the matching {@link CategoryResponseDTO}
     * @throws com.cts.productservice.exception.custom.ResourceNotFoundException if no category exists with the given name
     */
    @GetMapping("/categoryName/{categoryName}")
    public ResponseEntity<CategoryResponseDTO> byName(@PathVariable String categoryName) {
        log.info("GET category by name={}", categoryName);
        return ResponseEntity.ok(categoryService.findByName(categoryName));
    }

    /**
     * Updates an existing category. Only administrators may perform this operation.
     *
     * @param role       the caller's role from the {@code X-User-Role} header; must be {@code ADMIN}
     * @param categoryId the identifier of the category to update
     * @param dto        the {@link CategoryDTO} carrying the new details
     * @return {@code 200 OK} with the updated {@link CategoryResponseDTO}
     * @throws com.cts.productservice.exception.custom.UnauthorizedAccessException if the caller is not an admin
     * @throws com.cts.productservice.exception.custom.ResourceNotFoundException if no category exists with the given id
     * @throws com.cts.productservice.exception.custom.DuplicateResourceException if the new name collides with another category
     */
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> update(@RequestHeader(value = "X-User-Role", required = false) String role,
                                                      @PathVariable Long categoryId,
                                                      @Valid @RequestBody CategoryDTO dto) {
        log.info("PUT update categoryId={}", categoryId);
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(categoryService.update(categoryId, dto));
    }

    /**
     * Deletes a category by its identifier. Only administrators may perform this operation.
     *
     * @param role       the caller's role from the {@code X-User-Role} header; must be {@code ADMIN}
     * @param categoryId the identifier of the category to delete
     * @return {@code 204 NO CONTENT} when the category has been removed
     * @throws com.cts.productservice.exception.custom.UnauthorizedAccessException if the caller is not an admin
     * @throws com.cts.productservice.exception.custom.ResourceNotFoundException if no category exists with the given id
     * @throws com.cts.productservice.exception.custom.InvalidOperationException if products are still attached to the category
     */
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> delete(@RequestHeader(value = "X-User-Role", required = false) String role,
                                       @PathVariable Long categoryId) {
        log.info("DELETE categoryId={}", categoryId);
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        categoryService.delete(categoryId);
        return ResponseEntity.noContent().build();
    }
}
