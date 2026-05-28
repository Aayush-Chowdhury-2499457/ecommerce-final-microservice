package com.cts.reviewservice.service.impl;

import com.cts.reviewservice.client.HasPurchasedResponseDTO;
import com.cts.reviewservice.client.OrderServiceClient;
import com.cts.reviewservice.dto.CreateReviewDTO;
import com.cts.reviewservice.dto.ReviewResponseDTO;
import com.cts.reviewservice.dto.UpdateReviewDTO;
import com.cts.reviewservice.entity.Review;
import com.cts.reviewservice.exception.custom.DuplicateReviewException;
import com.cts.reviewservice.exception.custom.ForbiddenException;
import com.cts.reviewservice.exception.custom.PurchaseNotVerifiedException;
import com.cts.reviewservice.exception.custom.ResourceNotFoundException;
import com.cts.reviewservice.repository.ReviewRepository;
import com.cts.reviewservice.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private static final String ROLE_ADMIN = "ADMIN";

    private final ReviewRepository reviewRepository;
    private final OrderServiceClient orderServiceClient;

    /* ---------------- Create ---------------- */
    @Override
    @Transactional
    public ReviewResponseDTO create(Long callerUserId, CreateReviewDTO dto) {

        // 1. one review per (user, product)
        if (reviewRepository.existsByUserIdAndProductId(callerUserId, dto.getProductId())) {
            throw new DuplicateReviewException("You have already reviewed this product");
        }

        // 2. verify purchase via order-service (Feign + circuit breaker fallback)
        HasPurchasedResponseDTO hp = orderServiceClient.hasPurchased(callerUserId, dto.getProductId());
        if (hp == null || !hp.isHasPurchased()) {
            throw new PurchaseNotVerifiedException("You can only review products you have purchased");
        }

        // 3. cross-check the submitted orderId
        if (hp.getOrderId() != null && !hp.getOrderId().equals(dto.getOrderId())) {
            throw new PurchaseNotVerifiedException(
                    "Provided orderId does not match a purchase of this product");
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
    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> findAll() {
        return reviewRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponseDTO findById(Long reviewId) {
        return toDto(getOrThrow(reviewId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> findByUser(Long userId) {
        return reviewRepository.findByUserId(userId).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> findByProduct(Long productId) {
        return reviewRepository.findByProductId(productId).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Double averageRatingForProduct(Long productId) {
        Double avg = reviewRepository.averageRatingByProductId(productId);
        return avg == null ? 0.0 : avg;
    }

    /* ---------------- Update (owner only) ---------------- */
    @Override
    @Transactional
    public ReviewResponseDTO update(Long reviewId, Long callerUserId, String callerRole,
                                    UpdateReviewDTO dto) {
        Review review = getOrThrow(reviewId);

        if (!review.getUserId().equals(callerUserId)) {
            throw new ForbiddenException("You can only edit your own review");
        }

        review.setRating(dto.getRating());
        review.setDescription(dto.getDescription());
        return toDto(review);
    }

    /* ---------------- Delete (owner OR admin) ---------------- */
    @Override
    @Transactional
    public void delete(Long reviewId, Long callerUserId, String callerRole) {
        Review review = getOrThrow(reviewId);

        boolean isOwner = review.getUserId().equals(callerUserId);
        boolean isAdmin = ROLE_ADMIN.equalsIgnoreCase(callerRole);

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("You can only delete your own review");
        }
        reviewRepository.delete(review);
    }

    /* ---------------- helpers ---------------- */
    private Review getOrThrow(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));
    }

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