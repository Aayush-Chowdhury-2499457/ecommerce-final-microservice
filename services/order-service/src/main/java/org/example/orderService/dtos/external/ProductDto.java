package org.example.orderService.dtos.external;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long productId;
    private String productName;
    private Double price;
    private Integer stock;
}