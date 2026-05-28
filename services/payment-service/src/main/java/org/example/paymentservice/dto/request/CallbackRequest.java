package org.example.paymentservice.dto.request;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CallbackRequest {
    private String transactionId;
    private String status;
}