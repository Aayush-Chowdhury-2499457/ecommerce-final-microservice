package org.example.paymentservice.dto.external;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {
    private Long orderId;
    private Double amount;
    private String currency;
}