package com.cts.cartservice.dto.request;

import lombok.*;

/**
 * Request payload sent to the order-service to place an order at checkout.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceOrderDTO {
    private Long userId;
    private Long shoppingCartId;
    private Long addressId;
}
