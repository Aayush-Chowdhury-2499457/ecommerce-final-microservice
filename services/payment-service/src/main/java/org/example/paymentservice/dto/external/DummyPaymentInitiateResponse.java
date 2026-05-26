package org.example.paymentservice.dto.external;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class DummyPaymentInitiateResponse {
    private String transactionId;
    private String paymentLink;
    private String status;
}