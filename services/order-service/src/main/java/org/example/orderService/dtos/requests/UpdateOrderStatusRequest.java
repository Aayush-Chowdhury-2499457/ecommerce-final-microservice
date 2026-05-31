package org.example.orderService.dtos.requests;

import jakarta.validation.constraints.*;
import lombok.*;
import org.example.orderService.enums.OrderStatus;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {

    @NotNull(message = "Order status cannot be null")
    private OrderStatus orderStatus;
}