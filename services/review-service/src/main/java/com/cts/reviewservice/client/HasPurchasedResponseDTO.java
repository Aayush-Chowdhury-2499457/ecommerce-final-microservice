package com.cts.reviewservice.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HasPurchasedResponseDTO {

    private boolean hasPurchased;
    private Long orderId;
}