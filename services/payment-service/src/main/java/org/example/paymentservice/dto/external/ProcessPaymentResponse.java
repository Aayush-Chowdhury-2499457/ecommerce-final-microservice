package org.example.paymentservice.dto.external;

import lombok.*;

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