package com.cts.cartservice.dto.response;

import lombok.*;

/**
 * Response view of a single cart line item, including price and subtotal.
 */
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
