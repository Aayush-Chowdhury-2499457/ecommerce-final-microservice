package com.cts.orderservice.dto.external;

import lombok.*;

import java.util.List;

/**
 * External representation of a shopping cart fetched from the cart service.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCartDTO {

    private Long shoppingCartId;
    private Long userId;
    private List<CartItemDTO> cartItems;
    private Double totalPrice;
}
