package com.cts.reviewservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {

    private Long reviewId;
    private Long userId;
    private Long productId;
    private Long orderId;
    private Integer rating;
    private String description;
    private Boolean isVerifiedPurchase;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}