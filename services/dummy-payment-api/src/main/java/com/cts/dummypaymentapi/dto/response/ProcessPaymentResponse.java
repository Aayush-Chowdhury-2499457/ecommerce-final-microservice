package com.cts.dummypaymentapi.dto.response;

import lombok.*;

/**
 * Response payload returned after a payment has been processed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessPaymentResponse {
    private String transactionId;
    private Long orderId;
    private Double amount;
    private String currency;
    private String status;
    private String message;
}