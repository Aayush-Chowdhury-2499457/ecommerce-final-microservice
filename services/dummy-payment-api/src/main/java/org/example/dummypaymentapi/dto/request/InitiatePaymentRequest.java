package org.example.dummypaymentapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class InitiatePaymentRequest {
    private Long orderId;
    private Double amount;
    private String currency;
    private String callbackUrl;
}