package com.cts.orderservice.dto.external;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productId;
    private String productName;
    private Double price;
    private Integer stock;
}