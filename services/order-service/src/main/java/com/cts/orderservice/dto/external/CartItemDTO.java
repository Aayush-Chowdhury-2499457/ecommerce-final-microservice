package com.cts.orderservice.dto.external;

import lombok.*;

/**
 * External representation of a single cart line item from the cart service.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long cartItemId;
    private Long productId;
    private Integer quantity;
}
