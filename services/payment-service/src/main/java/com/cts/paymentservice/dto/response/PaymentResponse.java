package com.cts.paymentservice.dto.response;

import com.cts.paymentservice.enums.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO representing a persisted payment and its current status.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long paymentId;
    private Long userId;
    private Long orderId;
    private Double amount;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}