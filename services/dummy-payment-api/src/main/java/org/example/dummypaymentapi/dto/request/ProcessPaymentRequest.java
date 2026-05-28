package org.example.dummypaymentapi.dto.request;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {
    private Long orderId;
    private Double amount;
    private String currency;
}