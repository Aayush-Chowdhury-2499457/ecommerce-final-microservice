package com.cts.dummypaymentapi.dto.request;

import lombok.*;

/**
 * Request payload describing a payment to be processed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {
    private Long orderId;
    private Double amount;
    private String currency;
}