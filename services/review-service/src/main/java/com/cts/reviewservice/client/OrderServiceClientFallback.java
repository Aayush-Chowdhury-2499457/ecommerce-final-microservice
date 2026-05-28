package com.cts.reviewservice.client;

import com.cts.reviewservice.exception.custom.ServiceUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class OrderServiceClientFallback implements OrderServiceClient {

    @Override
    public HasPurchasedResponseDTO hasPurchased(Long userId, Long productId) {
        throw new ServiceUnavailableException("Cannot verify purchase at this time");
    }
}