package org.example.orderService.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {
    private Long userId;
    private Long cartId;
    private Long addressId;
}
