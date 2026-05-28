package com.cts.productservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private Long productId;
    private String productName;
    private String description;
    private Double price;
    private Integer stock;

    private Long categoryId;
    private String categoryName;

    private String imageUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
