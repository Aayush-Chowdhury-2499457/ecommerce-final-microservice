package com.cts.orderservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequestDTO {

    @NotNull
    private Long userId;

    @NotNull
    private Long shoppingCartId;

    @NotNull
    private Long addressId;
}
