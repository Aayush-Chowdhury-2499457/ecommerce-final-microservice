package com.cts.reviewservice.controller;

import com.cts.reviewservice.dto.CreateReviewDTO;
import com.cts.reviewservice.dto.ReviewResponseDTO;
import com.cts.reviewservice.dto.UpdateReviewDTO;
import com.cts.reviewservice.service.ReviewService;
import com.cts.reviewservice.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller exposing review CRUD and query endpoints under /api/reviews.
 */
@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /** Creates a review; restricted to CUSTOMER callers. */
    @PostMapping  // CUSTOMER only
    public ResponseEntity<ReviewResponseDTO> create(
            @RequestHeader("X-User-Id") Long callerUserId,
            @RequestHeader(value = "X-User-Role", required = false) String callerRole,
            @Valid @RequestBody CreateReviewDTO dto) {
        log.info("Create review request from user {}", callerUserId);
        AuthUtil.requireRole(callerRole, AuthUtil.ROLE_CUSTOMER);
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(callerUserId, dto));
    }

    /** Returns all reviews. */
    @GetMapping
    public ResponseEntity<List<ReviewResponseDTO>> all() {
        log.debug("Fetching all reviews");
        return ResponseEntity.ok(reviewService.findAll());
    }

    /** Returns the review with the given id. */
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> byId(@PathVariable Long reviewId) {
        log.debug("Fetching review {}", reviewId);
        return ResponseEntity.ok(reviewService.findById(reviewId));
    }

    /** Returns all reviews authored by the given user. */
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ReviewResponseDTO>> byUser(@PathVariable Long userId) {
        log.debug("Fetching reviews for user {}", userId);
        return ResponseEntity.ok(reviewService.findByUser(userId));
    }

    /** Returns all reviews for the given product. */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponseDTO>> byProduct(@PathVariable Long productId) {
        log.debug("Fetching reviews for product {}", productId);
        return ResponseEntity.ok(reviewService.findByProduct(productId));
    }

    /** Returns the average rating for the given product. */
    @GetMapping("/product/{productId}/average")
    public ResponseEntity<Map<String, Object>> averageForProduct(@PathVariable Long productId) {
        log.debug("Computing average rating for product {}", productId);
        Double avg = reviewService.averageRatingForProduct(productId);
        return ResponseEntity.ok(Map.of("productId", productId, "averageRating", avg));
    }

    /** Updates a review; restricted to the review's owner. */
    @PutMapping("/{reviewId}")  // owner only: fetch, authorize, then update
    public ResponseEntity<ReviewResponseDTO> update(
            @PathVariable Long reviewId,
            @RequestHeader("X-User-Id") Long callerUserId,
            @RequestHeader(value = "X-User-Role", required = false) String callerRole,
            @Valid @RequestBody UpdateReviewDTO dto) {
        log.info("Update review {} request from user {}", reviewId, callerUserId);
        ReviewResponseDTO existing = reviewService.findById(reviewId);
        AuthUtil.requireOwner(existing.getUserId(), callerUserId);
        return ResponseEntity.ok(reviewService.update(reviewId, dto));
    }

    /** Deletes a review; restricted to the owner or an admin. */
    @DeleteMapping("/{reviewId}")  // owner-or-admin: fetch, authorize, then delete
    public ResponseEntity<Void> delete(
            @PathVariable Long reviewId,
            @RequestHeader("X-User-Id") Long callerUserId,
            @RequestHeader(value = "X-User-Role", required = false) String callerRole) {
        log.info("Delete review {} request from user {}", reviewId, callerUserId);
        ReviewResponseDTO existing = reviewService.findById(reviewId);
        AuthUtil.requireSelfOrAdmin(existing.getUserId(), callerUserId, callerRole);
        reviewService.delete(reviewId);
        return ResponseEntity.noContent().build();
    }
}