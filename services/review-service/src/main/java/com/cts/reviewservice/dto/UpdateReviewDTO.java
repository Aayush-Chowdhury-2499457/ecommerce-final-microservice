package com.cts.reviewservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Request payload for updating an existing review's rating and description.
 */
@Data
public class UpdateReviewDTO {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @Size(max = 1000)
    private String description;
}