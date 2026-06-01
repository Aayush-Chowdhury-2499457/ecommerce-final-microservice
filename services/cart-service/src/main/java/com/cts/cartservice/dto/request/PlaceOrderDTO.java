package com.cts.cartservice.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceOrderDTO {
    private Long userId;
    private Long shoppingCartId;
    private Long addressId;
}
