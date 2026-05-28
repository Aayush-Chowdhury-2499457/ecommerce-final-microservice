package org.example.paymentservice.dto.external;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.paymentservice.enums.PaymentStatus;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentStatusRequest {
    private PaymentStatus paymentStatus;
}