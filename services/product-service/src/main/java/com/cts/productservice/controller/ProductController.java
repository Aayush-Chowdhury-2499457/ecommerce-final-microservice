package com.cts.productservice.controller;

import com.cts.productservice.dto.request.*;
import com.cts.productservice.dto.response.*;
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
 * REST controller exposing product catalog operations under {@code /api/products}.
 * <p>
 * Read operations are open to any caller, whereas mutating operations are restricted
 * to administrators, enforced through {@link AuthUtil} against the {@code X-User-Role}
 * header forwarded by the API Gateway. Business logic is delegated to
 * {@link ProductService}.
 *
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    /** Service that performs the product business operations. */
    private final ProductService productService;

    /**
     * Creates a new product. Only administrators may perform this operation.
     *
     * @param role the caller's role from the {@code X-User-Role} header; must be {@code ADMIN}
     * @param dto  the {@link CreateProductDTO} describing the product to create
     * @return {@code 201 CREATED} with the persisted {@link ProductResponseDTO}
     * @throws com.cts.productservice.exception.custom.UnauthorizedAccessException if the caller is not an admin
     */
    @PostMapping
    public ResponseEntity<ProductResponseDTO> create(@RequestHeader(value = "X-User-Role", required = false) String role,
                                                     @Valid @RequestBody CreateProductDTO dto) {
        log.info("POST create product name={} categoryId={}", dto.getProductName(), dto.getCategoryId());
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(dto));
    }

    /**
     * Returns every product in the catalog.
     *
     * @return {@code 200 OK} with the list of all {@link ProductResponseDTO}s
     */
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> all() {
        log.info("GET all products");
        return ResponseEntity.ok(productService.findAll());
    }

    /**
     * Returns a single product by its identifier.
     *
     * @param productId the identifier of the product to fetch
     * @return {@code 200 OK} with the matching {@link ProductResponseDTO}
     * @throws com.cts.productservice.exception.custom.ResourceNotFoundException if no product exists with the given id
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> byId(@PathVariable Long productId) {
        log.info("GET product by productId={}", productId);
        return ResponseEntity.ok(productService.findById(productId));
    }

    /**
     * Returns all products whose name contains the given text.
     *
     * @param productName the substring to match against product names
     * @return {@code 200 OK} with the matching {@link ProductResponseDTO}s
     */
    @GetMapping("/productName/{productName}")
    public ResponseEntity<List<ProductResponseDTO>> byName(@PathVariable String productName) {
        log.info("GET products by name={}", productName);
        return ResponseEntity.ok(productService.findByName(productName));
    }

    /**
     * Returns all products belonging to the given category.
     *
     * @param categoryId the identifier of the owning category
     * @return {@code 200 OK} with the matching {@link ProductResponseDTO}s
     * @throws com.cts.productservice.exception.custom.ResourceNotFoundException if no category exists with the given id
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponseDTO>> byCategory(@PathVariable Long categoryId) {
        log.info("GET products by categoryId={}", categoryId);
        return ResponseEntity.ok(productService.findByCategory(categoryId));
    }

    /**
     * Updates an existing product. Only administrators may perform this operation.
     *
     * @param role      the caller's role from the {@code X-User-Role} header; must be {@code ADMIN}
     * @param productId the identifier of the product to update
     * @param dto       the {@link UpdateProductDTO} carrying the fields to change
     * @return {@code 200 OK} with the updated {@link ProductResponseDTO}
     * @throws com.cts.productservice.exception.custom.UnauthorizedAccessException if the caller is not an admin
     * @throws com.cts.productservice.exception.custom.ResourceNotFoundException if the product or target category does not exist
     */
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> update(@RequestHeader(value = "X-User-Role", required = false) String role,
                                                     @PathVariable Long productId,
                                                     @Valid @RequestBody UpdateProductDTO dto) {
        log.info("PUT update productId={}", productId);
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(productService.update(productId, dto));
    }

    /**
     * Sets the absolute stock quantity for a product. Only administrators may perform
     * this operation.
     *
     * @param role      the caller's role from the {@code X-User-Role} header; must be {@code ADMIN}
     * @param productId the identifier of the product to adjust
     * @param dto       the {@link StockQuantityDTO} carrying the new absolute quantity
     * @return {@code 200 OK} with the updated {@link ProductResponseDTO}
     * @throws com.cts.productservice.exception.custom.UnauthorizedAccessException if the caller is not an admin
     * @throws com.cts.productservice.exception.custom.ResourceNotFoundException if no product exists with the given id
     */
    @PatchMapping("/{productId}/stock")
    public ResponseEntity<ProductResponseDTO> updateStock(@RequestHeader(value = "X-User-Role", required = false) String role,
                                                          @PathVariable Long productId,
                                                          @Valid @RequestBody StockQuantityDTO dto) {
        log.info("PATCH update stock productId={} quantity={}", productId, dto.getQuantity());
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(productService.updateStock(productId, dto));
    }

    /**
     * Reduces the stock of a product by the given quantity. Intended for internal
     * order-fulfilment calls; the admin role is enforced only when a role is present.
     *
     * @param role      the caller's role from the {@code X-User-Role} header; checked only if present
     * @param productId the identifier of the product to adjust
     * @param dto       the {@link StockQuantityDTO} carrying the quantity to subtract
     * @return {@code 200 OK} with the updated {@link ProductResponseDTO}
     * @throws com.cts.productservice.exception.custom.UnauthorizedAccessException if a role is present but is not admin
     * @throws com.cts.productservice.exception.custom.ResourceNotFoundException if no product exists with the given id
     * @throws com.cts.productservice.exception.custom.InvalidOperationException if available stock is insufficient
     */
    @PutMapping("/{productId}/reduce-stock")
    public ResponseEntity<ProductResponseDTO> reduceStock(@RequestHeader(value = "X-User-Role", required = false) String role,
                                                          @PathVariable Long productId, @Valid @RequestBody StockQuantityDTO dto) {
        log.info("PUT reduce stock productId={} quantity={}", productId, dto.getQuantity());
        AuthUtil.requireRoleIfPresent(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(productService.reduceStock(productId, dto));
    }

    /**
     * Deletes a product by its identifier. Only administrators may perform this operation.
     *
     * @param role      the caller's role from the {@code X-User-Role} header; must be {@code ADMIN}
     * @param productId the identifier of the product to delete
     * @return {@code 204 NO CONTENT} when the product has been removed
     * @throws com.cts.productservice.exception.custom.UnauthorizedAccessException if the caller is not an admin
     * @throws com.cts.productservice.exception.custom.ResourceNotFoundException if no product exists with the given id
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@RequestHeader(value = "X-User-Role", required = false) String role,
                                       @PathVariable Long productId) {
        log.info("DELETE productId={}", productId);
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        productService.delete(productId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Increases the stock of a product by the given quantity. Intended for internal
     * restocking calls; the admin role is enforced only when a role is present.
     *
     * @param role      the caller's role from the {@code X-User-Role} header; checked only if present
     * @param productId the identifier of the product to adjust
     * @param dto       the {@link StockQuantityDTO} carrying the quantity to add
     * @return {@code 200 OK} with the updated {@link ProductResponseDTO}
     * @throws com.cts.productservice.exception.custom.UnauthorizedAccessException if a role is present but is not admin
     * @throws com.cts.productservice.exception.custom.ResourceNotFoundException if no product exists with the given id
     */
    @PutMapping("/{productId}/restock")
    public ResponseEntity<ProductResponseDTO> restock(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long productId, @Valid @RequestBody StockQuantityDTO dto) {
        log.info("PUT restock productId={} quantity={}", productId, dto.getQuantity());
        AuthUtil.requireRoleIfPresent(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(productService.restock(productId, dto));
    }
}
