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

/**
 * Resilient gateway to the product service. Wraps the Feign client with a rate limiter,
 * retry, and circuit breaker for product lookups and stock adjustments.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceGateway {

    private static final String PRODUCT_SERVICE_CB = "productService";

    private final ProductServiceClient productServiceClient;

    /**
     * Fetches product details by id.
     *
     * @param productId the product id
     * @return the product details
     */
    @RateLimiter(name = PRODUCT_SERVICE_CB)
    @Retry(name = PRODUCT_SERVICE_CB)
    @CircuitBreaker(name = PRODUCT_SERVICE_CB, fallbackMethod = "fetchProductFallback")
    public ProductDTO fetchProduct(Long productId) {
        return productServiceClient.getProductById(productId);
    }

    /**
     * Fallback for {@link #fetchProduct(Long)}: maps a 404 to {@link ResourceNotFoundException}
     * and any other failure to {@link ServiceUnavailableException}.
     *
     * @param productId the product id
     * @param ex        the triggering throwable
     * @return never returns normally; always throws
     */
    public ProductDTO fetchProductFallback(Long productId, Throwable ex) {
        if (ex instanceof DownstreamException de && de.getStatusCode() == 404) {
            throw new ResourceNotFoundException("Product not found for id=" + productId);
        }
        log.error("Product Service Fallback (fetchProduct): {}", ex.getMessage());
        throw new ServiceUnavailableException("Product Service Unavailable, please try again later");
    }

    /**
     * Reduces the stock of a product by the given quantity (at order placement).
     *
     * @param productId the product id
     * @param quantity  the quantity to deduct
     */
    @RateLimiter(name = PRODUCT_SERVICE_CB)
    @Retry(name = PRODUCT_SERVICE_CB)
    @CircuitBreaker(name = PRODUCT_SERVICE_CB, fallbackMethod = "reduceStockFallback")
    public void reduceStock(Long productId, Integer quantity) {
        productServiceClient.reduceStock(productId, new ReduceStockDTO(quantity));
    }

    /**
     * Fallback for {@link #reduceStock(Long, Integer)}: re-throws downstream 404/400 errors
     * (e.g. insufficient stock) and maps anything else to {@link ServiceUnavailableException}.
     *
     * @param productId the product id
     * @param quantity  the requested quantity
     * @param ex        the triggering throwable
     * @return never returns normally; always throws
     */
    public ProductDTO reduceStockFallback(Long productId, Integer quantity, Throwable ex) {
        if (ex instanceof DownstreamException de) throw de;   // 404 / 400 (insufficient stock)
        log.error("Product Service Fallback (reduceStock): {}", ex.getMessage());
        throw new ServiceUnavailableException("Product Service Unavailable, please try again later");
    }

    /**
     * Restores stock for a product by the given quantity (on order cancellation).
     *
     * @param productId the product id
     * @param quantity  the quantity to restore
     */
    @RateLimiter(name = PRODUCT_SERVICE_CB)
    @Retry(name = PRODUCT_SERVICE_CB)
    @CircuitBreaker(name = PRODUCT_SERVICE_CB, fallbackMethod = "restockFallback")
    public void restock(Long productId, Integer quantity) {
        productServiceClient.restock(productId, new ReduceStockDTO(quantity));
    }

    /**
     * Fallback for {@link #restock(Long, Integer)}: logs the failure and returns {@code null}
     * so that cancellation is not blocked by a transient restock error.
     *
     * @param productId the product id
     * @param quantity  the quantity that failed to restore
     * @param ex        the triggering throwable
     * @return always {@code null}
     */
    public ProductDTO restockFallback(Long productId, Integer quantity, Throwable ex) {
        log.error("Failed to restock product {} (qty {}): {}", productId, quantity, ex.getMessage());
        return null;
    }
}
