package com.cts.paymentservice.dto.external;

import lombok.*;

/**
 * Outbound request sent to the dummy payment provider to process a charge.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {
    private Long orderId;
    private Double amount;
    private String currency;
}