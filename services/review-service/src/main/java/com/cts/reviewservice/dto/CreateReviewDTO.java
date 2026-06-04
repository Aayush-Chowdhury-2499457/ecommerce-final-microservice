package com.cts.reviewservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Request payload for creating a new product review.
 */
@Data
public class CreateReviewDTO {

    @NotNull
    private Long productId;

    @NotNull
    private Long orderId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @Size(max = 1000)
    private String description;
}