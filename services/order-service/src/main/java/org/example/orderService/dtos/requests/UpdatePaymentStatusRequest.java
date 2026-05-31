package org.example.orderService.dtos.requests;

import jakarta.validation.constraints.*;
import lombok.*;
import org.example.orderService.enums.PaymentStatus;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentStatusRequest {

    @NotNull(message = "Payment status cannot be null")
    private PaymentStatus paymentStatus;
}