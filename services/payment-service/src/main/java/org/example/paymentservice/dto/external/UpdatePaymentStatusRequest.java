package org.example.paymentservice.dto.external;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentStatusRequest {
    private String paymentStatus;
}