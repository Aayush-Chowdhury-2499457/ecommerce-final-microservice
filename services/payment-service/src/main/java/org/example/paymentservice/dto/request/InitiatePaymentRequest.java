package org.example.paymentservice.dto.request;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class InitiatePaymentRequest {
    private Long orderId;
    private Double amount;
}