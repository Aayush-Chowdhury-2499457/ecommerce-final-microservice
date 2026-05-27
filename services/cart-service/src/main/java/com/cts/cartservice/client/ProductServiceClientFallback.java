package com.cts.cartservice.client;

import com.cts.cartservice.dto.ProductDTO;
import com.cts.cartservice.exception.custom.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductServiceClientFallback implements ProductServiceClient {

    @Override
    public ProductDTO getProductById(Long productId) {
        log.error("Fallback triggered for getProductById({})", productId);
        throw new ServiceUnavailableException("Product Service unavailable, cannot add/update item " + productId);
    }
}
