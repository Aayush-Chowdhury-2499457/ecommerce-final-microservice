package com.cts.orderservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.cts.orderservice.enums.PaymentStatus;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentStatusRequestDTO {
    private PaymentStatus paymentStatus;
}