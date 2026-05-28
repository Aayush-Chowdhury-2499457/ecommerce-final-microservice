package org.example.orderService.dtos.external;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private Long cartId;
    private Long userId;
    private Boolean isActive;
    private List<CartItemDto> cartItems;
}
