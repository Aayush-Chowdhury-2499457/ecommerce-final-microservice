package org.example.dummypaymentapi.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionStatusResponse {
    private String transactionId;
    private Long orderId;
    private Double amount;
    private String status;
    private LocalDateTime createdAt;
}