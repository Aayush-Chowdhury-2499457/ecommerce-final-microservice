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
        ResponseEntity<ProductDTO> response = productServiceClient.getProductById(productId);
        return response.getBody();
    }

    public ProductDTO fetchProductFallback(Long productId, Throwable ex) {
        if (ex instanceof DownstreamException de) {
            throw new DownstreamException("Product not found for id=" + productId, HttpStatus.NOT_FOUND);
        }
        log.error("Product Service Fallback: {}", ex.getMessage());
        throw new ServiceUnavailableException("Product Service Unavailable, please try again later");
    }

}
