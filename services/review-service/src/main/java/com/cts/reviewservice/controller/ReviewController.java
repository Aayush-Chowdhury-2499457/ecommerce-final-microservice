package com.cts.reviewservice.controller;

import com.cts.reviewservice.dto.CreateReviewDTO;
import com.cts.reviewservice.dto.ReviewResponseDTO;
import com.cts.reviewservice.dto.UpdateReviewDTO;
import com.cts.reviewservice.service.ReviewService;
import com.cts.reviewservice.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping  // CUSTOMER only
    public ResponseEntity<ReviewResponseDTO> create(
            @RequestHeader("X-User-Id") Long callerUserId,
            @RequestHeader(value = "X-User-Role", required = false) String callerRole,
            @Valid @RequestBody CreateReviewDTO dto) {
        AuthUtil.requireRole(callerRole, AuthUtil.ROLE_CUSTOMER);
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(callerUserId, dto));
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponseDTO>> all() {
        return ResponseEntity.ok(reviewService.findAll());
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> byId(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.findById(reviewId));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ReviewResponseDTO>> byUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.findByUser(userId));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponseDTO>> byProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.findByProduct(productId));
    }

    @GetMapping("/product/{productId}/average")
    public ResponseEntity<Map<String, Object>> averageForProduct(@PathVariable Long productId) {
        Double avg = reviewService.averageRatingForProduct(productId);
        return ResponseEntity.ok(Map.of("productId", productId, "averageRating", avg));
    }

    @PutMapping("/{reviewId}")  // owner only: fetch, authorize, then update
    public ResponseEntity<ReviewResponseDTO> update(
            @PathVariable Long reviewId,
            @RequestHeader("X-User-Id") Long callerUserId,
            @RequestHeader(value = "X-User-Role", required = false) String callerRole,
            @Valid @RequestBody UpdateReviewDTO dto) {
        ReviewResponseDTO existing = reviewService.findById(reviewId);
        AuthUtil.requireOwner(existing.getUserId(), callerUserId);
        return ResponseEntity.ok(reviewService.update(reviewId, dto));
    }

    @DeleteMapping("/{reviewId}")  // owner-or-admin: fetch, authorize, then delete
    public ResponseEntity<Void> delete(
            @PathVariable Long reviewId,
            @RequestHeader("X-User-Id") Long callerUserId,
            @RequestHeader(value = "X-User-Role", required = false) String callerRole) {
        ReviewResponseDTO existing = reviewService.findById(reviewId);
        AuthUtil.requireSelfOrAdmin(existing.getUserId(), callerUserId, callerRole);
        reviewService.delete(reviewId);
        return ResponseEntity.noContent().build();
    }
}