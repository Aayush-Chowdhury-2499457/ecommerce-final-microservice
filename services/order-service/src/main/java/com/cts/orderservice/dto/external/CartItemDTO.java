package com.cts.orderservice.dto.external;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long cartItemId;
    private Long productId;
    private Integer quantity;
}
