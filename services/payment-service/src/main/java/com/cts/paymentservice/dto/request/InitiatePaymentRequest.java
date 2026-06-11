package com.cts.paymentservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * Request body for initiating a payment: the target order and the amount to charge.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class InitiatePaymentRequest {
    @NotNull(message = "Order Id is required")
    private Long orderId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private Double amount;
}