package com.cts.cartservice.dto.response;

import lombok.*;

import java.util.List;

/**
 * Response view of a shopping cart with its items and computed total price.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingCartResponseDTO {

    private Long shoppingCartId;
    private Long userId;
    private List<CartItemResponseDTO> cartItems;
    private Double totalPrice;
}
