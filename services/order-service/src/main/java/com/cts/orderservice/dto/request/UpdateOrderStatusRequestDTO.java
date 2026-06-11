package com.cts.orderservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.cts.orderservice.enums.OrderStatus;

/**
 * Request payload for updating an order's status.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequestDTO {
    @NotNull
    private OrderStatus orderStatus;
}
