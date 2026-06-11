package com.cts.paymentservice.dto.external;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Outbound request to order-service carrying the new payment status for an order.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentStatusRequest {
    private String paymentStatus;
}