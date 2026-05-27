package com.cts.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderDTO {

    private Long userId;
    private Long shoppingCartId;
    private Long addressId;
}
