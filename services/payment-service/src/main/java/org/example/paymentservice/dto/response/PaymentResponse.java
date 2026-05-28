package org.example.paymentservice.dto.response;

import org.example.paymentservice.enums.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long paymentId;
    private Long orderId;
    private Double amount;
    private PaymentStatus paymentStatus;
    private String paymentLink;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}