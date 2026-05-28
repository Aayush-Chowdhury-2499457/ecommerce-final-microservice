package com.cts.reviewservice.controller;

import com.cts.reviewservice.dto.CreateReviewDTO;
import com.cts.reviewservice.dto.ReviewResponseDTO;
import com.cts.reviewservice.dto.UpdateReviewDTO;
import com.cts.reviewservice.service.ReviewService;
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

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> create(
            @RequestHeader("X-User-Id") Long callerUserId,
            @Valid @RequestBody CreateReviewDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.create(callerUserId, dto));
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

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> update(
            @PathVariable Long reviewId,
            @RequestHeader("X-User-Id") Long callerUserId,
            @RequestHeader(value = "X-User-Role", required = false) String callerRole,
            @Valid @RequestBody UpdateReviewDTO dto) {
        return ResponseEntity.ok(reviewService.update(reviewId, callerUserId, callerRole, dto));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long reviewId,
            @RequestHeader("X-User-Id") Long callerUserId,
            @RequestHeader(value = "X-User-Role", required = false) String callerRole) {
        reviewService.delete(reviewId, callerUserId, callerRole);
        return ResponseEntity.noContent().build();
    }
}