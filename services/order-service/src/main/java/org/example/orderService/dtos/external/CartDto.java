package org.example.orderService.dtos.external;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {

    @NotNull(message = "Shopping cart ID cannot be null")
    private Long shoppingCartId;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Cart items cannot be null")
    @NotEmpty(message = "Cart must have at least one item")
    @Valid
    private List<CartItemDto> cartItems;

    private Double totalPrice;
}