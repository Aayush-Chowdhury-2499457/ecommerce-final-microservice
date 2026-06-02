package com.cts.reviewservice.service;

import com.cts.reviewservice.dto.CreateReviewDTO;
import com.cts.reviewservice.dto.ReviewResponseDTO;
import com.cts.reviewservice.dto.UpdateReviewDTO;

import java.util.List;

/**
 * Business operations for creating, querying, updating and deleting reviews.
 */
public interface ReviewService {

    /** Creates a new review for the given caller after verifying eligibility. */
    ReviewResponseDTO create(Long callerUserId, CreateReviewDTO dto);

    /** Returns all reviews. */
    List<ReviewResponseDTO> findAll();

    /** Returns the review with the given id, or throws if not found. */
    ReviewResponseDTO findById(Long reviewId);

    /** Returns all reviews authored by the given user. */
    List<ReviewResponseDTO> findByUser(Long userId);

    /** Returns all reviews for the given product. */
    List<ReviewResponseDTO> findByProduct(Long productId);

    /** Returns the average rating for the given product. */
    Double averageRatingForProduct(Long productId);

    /** Updates the rating and description of the given review. */
    ReviewResponseDTO update(Long reviewId, UpdateReviewDTO dto);

    /** Deletes the review with the given id. */
    void delete(Long reviewId);
}