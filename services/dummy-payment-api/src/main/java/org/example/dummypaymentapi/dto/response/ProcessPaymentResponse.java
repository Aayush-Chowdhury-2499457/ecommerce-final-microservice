package org.example.dummypaymentapi.dto.response;

import lombok.*;

@Getter @Setter
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