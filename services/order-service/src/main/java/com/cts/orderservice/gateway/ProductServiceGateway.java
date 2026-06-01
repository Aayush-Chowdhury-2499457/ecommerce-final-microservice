package com.cts.orderservice.gateway;

import com.cts.orderservice.client.ProductServiceClient;
import com.cts.orderservice.dto.external.ProductDTO;
import com.cts.orderservice.dto.external.ReduceStockDTO;
import com.cts.orderservice.exception.custom.DownstreamException;
import com.cts.orderservice.exception.custom.ResourceNotFoundException;
import com.cts.orderservice.exception.custom.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
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

    @RateLimiter(name = PRODUCT_SERVICE_CB)
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

    @RateLimiter(name = PRODUCT_SERVICE_CB)
    @Retry(name = PRODUCT_SERVICE_CB)
    @CircuitBreaker(name = PRODUCT_SERVICE_CB, fallbackMethod = "reduceStockFallback")
    public void reduceStock(Long productId, Integer quantity) {
        productServiceClient.reduceStock(productId, new ReduceStockDTO(quantity));
    }
    public ProductDTO reduceStockFallback(Long productId, Integer quantity, Throwable ex) {
        if (ex instanceof DownstreamException de) throw de;   // 404 / 400 (insufficient stock)
        log.error("Product Service Fallback (reduceStock): {}", ex.getMessage());
        throw new ServiceUnavailableException("Product Service Unavailable, please try again later");
    }

    @RateLimiter(name = PRODUCT_SERVICE_CB)
    @Retry(name = PRODUCT_SERVICE_CB)
    @CircuitBreaker(name = PRODUCT_SERVICE_CB, fallbackMethod = "restockFallback")
    public void restock(Long productId, Integer quantity) {
        productServiceClient.restock(productId, new ReduceStockDTO(quantity));
    }

    public ProductDTO restockFallback(Long productId, Integer quantity, Throwable ex) {
        log.error("Failed to restock product {} (qty {}): {}", productId, quantity, ex.getMessage());
        return null;
    }
}
