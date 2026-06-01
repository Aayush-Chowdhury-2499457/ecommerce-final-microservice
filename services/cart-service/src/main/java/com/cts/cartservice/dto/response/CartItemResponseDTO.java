package com.cts.cartservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDTO {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private Double unitPrice;
    private Integer quantity;
    private Double subTotal;
}
