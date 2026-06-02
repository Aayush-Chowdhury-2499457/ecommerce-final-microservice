package com.cts.reviewservice.service.impl;

import com.cts.reviewservice.dto.CreateReviewDTO;
import com.cts.reviewservice.dto.ReviewResponseDTO;
import com.cts.reviewservice.dto.UpdateReviewDTO;
import com.cts.reviewservice.entity.Review;
import com.cts.reviewservice.exception.custom.DuplicateReviewException;
import com.cts.reviewservice.exception.custom.PurchaseNotVerifiedException;
import com.cts.reviewservice.exception.custom.ResourceNotFoundException;
import com.cts.reviewservice.gateway.OrderServiceGateway;
import com.cts.reviewservice.repository.ReviewRepository;
import com.cts.reviewservice.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default {@link ReviewService} implementation backed by the JPA repository
 * and the order-service gateway for purchase verification.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderServiceGateway orderServiceGateway;

    /* ---------------- Create ---------------- */
    /** Creates a review after enforcing uniqueness and verified purchase. */
    @Override
    @Transactional
    public ReviewResponseDTO create(Long callerUserId, CreateReviewDTO dto) {
        log.info("Creating review for user {} and product {}", callerUserId, dto.getProductId());

        // 1. one review per (user, product)
        if (reviewRepository.existsByUserIdAndProductId(callerUserId, dto.getProductId())) {
            log.warn("Duplicate review attempt by user {} for product {}", callerUserId, dto.getProductId());
            throw new DuplicateReviewException("You have already reviewed this product");
        }

        // 2. verify purchase via order-service (Feign + circuit breaker fallback)
        Boolean purchased = orderServiceGateway.hasPurchased(callerUserId, dto.getProductId());
        if (purchased == null || !purchased) {
            log.warn("Purchase not verified for user {} and product {}", callerUserId, dto.getProductId());
            throw new PurchaseNotVerifiedException("You can only review products you have purchased");
        }

        Review review = Review.builder()
                .userId(callerUserId)
                .productId(dto.getProductId())
                .orderId(dto.getOrderId())
                .rating(dto.getRating())
                .description(dto.getDescription())
                .isVerifiedPurchase(true)
                .build();

        return toDto(reviewRepository.save(review));
    }

    /* ---------------- Read ---------------- */
    /** Returns all reviews mapped to DTOs. */
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> findAll() {
        log.debug("Fetching all reviews");
        return reviewRepository.findAll().stream().map(this::toDto).toList();
    }

    /** Returns the review with the given id. */
    @Override
    @Transactional(readOnly = true)
    public ReviewResponseDTO findById(Long reviewId) {
        log.debug("Fetching review {}", reviewId);
        return toDto(getOrThrow(reviewId));
    }

    /** Returns all reviews authored by the given user. */
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> findByUser(Long userId) {
        log.debug("Fetching reviews for user {}", userId);
        return reviewRepository.findByUserId(userId).stream().map(this::toDto).toList();
    }

    /** Returns all reviews for the given product. */
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> findByProduct(Long productId) {
        log.debug("Fetching reviews for product {}", productId);
        return reviewRepository.findByProductId(productId).stream().map(this::toDto).toList();
    }

    /** Returns the average rating for the product, defaulting to zero. */
    @Override
    @Transactional(readOnly = true)
    public Double averageRatingForProduct(Long productId) {
        log.debug("Computing average rating for product {}", productId);
        Double avg = reviewRepository.averageRatingByProductId(productId);
        return avg == null ? 0.0 : avg;
    }

    /* ---------------- Update (owner only) ---------------- */
    /** Updates the rating and description of an existing review. */
    @Override
    @Transactional
    public ReviewResponseDTO update(Long reviewId, UpdateReviewDTO dto) {
        log.info("Updating review {}", reviewId);
        Review review = getOrThrow(reviewId);
        review.setRating(dto.getRating());
        review.setDescription(dto.getDescription());
        return toDto(review);
    }

    /* ---------------- Delete (owner OR admin) ---------------- */
    /** Deletes the review with the given id. */
    @Override
    @Transactional
    public void delete(Long reviewId) {
        log.info("Deleting review {}", reviewId);
        Review review = getOrThrow(reviewId);
        reviewRepository.delete(review);
    }

    /* ---------------- helpers ---------------- */
    /** Loads the review or throws {@link ResourceNotFoundException} if absent. */
    private Review getOrThrow(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));
    }

    /** Maps a {@link Review} entity to its response DTO. */
    private ReviewResponseDTO toDto(Review r) {
        return ReviewResponseDTO.builder()
                .reviewId(r.getReviewId())
                .userId(r.getUserId())
                .productId(r.getProductId())
                .orderId(r.getOrderId())
                .rating(r.getRating())
                .description(r.getDescription())
                .isVerifiedPurchase(r.getIsVerifiedPurchase())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .createdBy(r.getCreatedBy())
                .updatedBy(r.getUpdatedBy())
                .build();
    }
}