package com.cts.orderservice.gateway;

import com.cts.orderservice.client.ProductServiceClient;
import com.cts.orderservice.dto.external.ProductDTO;
import com.cts.orderservice.dto.external.ReduceStockDTO;
import com.cts.orderservice.exception.custom.DownstreamException;
import com.cts.orderservice.exception.custom.ResourceNotFoundException;
import com.cts.orderservice.exception.custom.ServiceUnavailableException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceGateway {

    private static final String PRODUCT_SERVICE_CB = "productService";

    private final ProductServiceClient productServiceClient;

    @Retry(name = PRODUCT_SERVICE_CB)
    @CircuitBreaker(name = PRODUCT_SERVICE_CB, fallbackMethod = "fetchProductFallback")
    public ProductDTO fetchProduct(Long productId) {
        return productServiceClient.getProductById(productId);
    }

    public ProductDTO fetchProductFallback(Long productId, Throwable ex) {
        if (ex instanceof DownstreamException de && de.getStatusCode() == 404) {
            throw new ResourceNotFoundException("Product not found for id=" + productId);
        }
        log.error("Product Service Fallback (fetchProduct): {}", ex.getMessage());
        throw new ServiceUnavailableException("Product Service Unavailable, please try again later");
    }

    @Retry(name = PRODUCT_SERVICE_CB)
    @CircuitBreaker(name = PRODUCT_SERVICE_CB, fallbackMethod = "reduceStockFallback")
    public ProductDTO reduceStock(Long productId, Integer quantity) {
        return productServiceClient.reduceStock(productId, new ReduceStockDTO(quantity));
    }
    public ProductDTO reduceStockFallback(Long productId, Integer quantity, Throwable ex) {
        if (ex instanceof DownstreamException de) throw de;   // 404 / 400 (insufficient stock)
        log.error("Product Service Fallback (reduceStock): {}", ex.getMessage());
        throw new ServiceUnavailableException("Product Service Unavailable, please try again later");
    }
}
