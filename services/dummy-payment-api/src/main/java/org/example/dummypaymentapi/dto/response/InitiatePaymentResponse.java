package org.example.dummypaymentapi.dto.response;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiatePaymentResponse {
    private String transactionId;
    private String paymentLink;
    private String status;
}