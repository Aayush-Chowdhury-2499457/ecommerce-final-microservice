package com.cts.orderservice.client;

import com.cts.orderservice.config.FeignConfig;
import com.cts.orderservice.dto.external.ProductDTO;
import com.cts.orderservice.dto.external.ReduceStockDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign client for the product service.
 */
@FeignClient(name = "product-service", configuration = FeignConfig.class)
public interface ProductServiceClient {

    /** Retrieves product details by id. */
    @GetMapping("/api/products/{productId}")
    ProductDTO getProductById(@PathVariable("productId") Long productId);

    /** Reduces stock for a product by the requested quantity. */
    @PutMapping("/api/products/{productId}/reduce-stock")
    ProductDTO reduceStock(@PathVariable("productId") Long productId, @RequestBody ReduceStockDTO body);

    /** Restores stock for a product by the requested quantity. */
    @PutMapping("/api/products/{productId}/restock")
    ProductDTO restock(@PathVariable("productId") Long productId, @RequestBody ReduceStockDTO body);
}
