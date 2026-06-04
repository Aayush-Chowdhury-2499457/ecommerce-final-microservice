package com.cts.cartservice.dto.response;

import lombok.*;

/**
 * Read-only view of a product fetched from the product-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long productId;
    private String productName;
    private String description;
    private Double price;
    private Integer stock;
    private Long categoryId;
    private String categoryName;
    private String imageUrl;
}
