package com.cts.paymentservice.dto.external;

import lombok.*;

/**
 * Inbound response from the dummy payment provider describing the charge outcome.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentResponse {
    private String transactionId;
    private Long orderId;
    private Double amount;
    private String currency;
    private String status;
    private String message;
}