package com.cts.dummypaymentapi.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {
    private Long orderId;
    private Double amount;
    private String currency;
}