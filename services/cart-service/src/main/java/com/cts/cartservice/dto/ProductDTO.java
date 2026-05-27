package com.cts.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
