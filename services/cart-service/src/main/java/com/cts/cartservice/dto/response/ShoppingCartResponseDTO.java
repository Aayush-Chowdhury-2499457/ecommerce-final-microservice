package com.cts.cartservice.dto.response;

import lombok.*;

import java.util.List;

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
