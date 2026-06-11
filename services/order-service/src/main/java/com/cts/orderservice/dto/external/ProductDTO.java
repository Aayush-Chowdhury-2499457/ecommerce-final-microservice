package com.cts.orderservice.dto.external;

import lombok.*;

/**
 * External representation of a product fetched from the product service.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productId;
    private String productName;
    private Double price;
    private Integer stock;
}