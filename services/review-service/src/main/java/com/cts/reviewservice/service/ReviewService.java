package com.cts.reviewservice.service;

import com.cts.reviewservice.dto.CreateReviewDTO;
import com.cts.reviewservice.dto.ReviewResponseDTO;
import com.cts.reviewservice.dto.UpdateReviewDTO;

import java.util.List;

public interface ReviewService {

    ReviewResponseDTO create(Long callerUserId, CreateReviewDTO dto);

    List<ReviewResponseDTO> findAll();

    ReviewResponseDTO findById(Long reviewId);

    List<ReviewResponseDTO> findByUser(Long userId);

    List<ReviewResponseDTO> findByProduct(Long productId);

    Double averageRatingForProduct(Long productId);

    ReviewResponseDTO update(Long reviewId, UpdateReviewDTO dto);

    void delete(Long reviewId);
}