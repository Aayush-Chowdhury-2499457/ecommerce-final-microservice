package com.cts.cartservice.gateway;

import com.cts.cartservice.client.ProductServiceClient;
import com.cts.cartservice.dto.response.ProductDTO;
import com.cts.cartservice.exception.custom.DownstreamException;
import com.cts.cartservice.exception.custom.ServiceUnavailableException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Resilience-wrapped gateway for fetching product details from the product-service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceGateway {

    private static final String PRODUCT_SERVICE_CB = "productService";

    private final ProductServiceClient productServiceClient;

    /** Fetches a product by id, guarded by rate limiter, retry, and circuit breaker. */
    @RateLimiter(name = PRODUCT_SERVICE_CB)
    @Retry(name = PRODUCT_SERVICE_CB)
    @CircuitBreaker(name = PRODUCT_SERVICE_CB, fallbackMethod = "fetchProductFallback")
    public ProductDTO fetchProduct(Long productId) {
        log.debug("Calling product-service to fetch productId={}", productId);
        ResponseEntity<ProductDTO> response = productServiceClient.getProductById(productId);
        return response.getBody();
    }

    /** Fallback invoked when the product-service call fails; surfaces a downstream or unavailable error. */
    public ProductDTO fetchProductFallback(Long productId, Throwable ex) {
        if (ex instanceof DownstreamException de) {
            throw new DownstreamException("Product not found for id=" + productId, HttpStatus.NOT_FOUND);
        }
        log.error("Product Service Fallback: {}", ex.getMessage());
        throw new ServiceUnavailableException("Product Service Unavailable, please try again later");
    }

}
