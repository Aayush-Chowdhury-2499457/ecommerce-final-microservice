package org.example.orderService.dtos.requests;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {


    private Long userId;

    @NotNull(message = "Address ID cannot be null")
    private Long addressId;
}