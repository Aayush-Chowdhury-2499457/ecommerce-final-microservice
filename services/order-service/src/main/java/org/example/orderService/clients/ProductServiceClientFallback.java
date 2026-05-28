package org.example.orderService.clients;


import org.example.orderService.dtos.external.ProductDto;
import org.example.orderService.exceptions.ServiceUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class ProductServiceClientFallback implements ProductServiceClient {

    @Override
    public ProductDto getProductById(Long productId) {
        throw new ServiceUnavailableException("Product service unavailable, cannot place order");
    }
}
