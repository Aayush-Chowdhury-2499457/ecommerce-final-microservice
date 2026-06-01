package com.cts.orderservice.client;

import com.cts.orderservice.config.FeignConfig;
import com.cts.orderservice.dto.external.ProductDTO;
import com.cts.orderservice.dto.external.ReduceStockDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service", configuration = FeignConfig.class)
public interface ProductServiceClient {

    @GetMapping("/api/products/{productId}")
    ProductDTO getProductById(@PathVariable("productId") Long productId);

    @PutMapping("/api/products/{productId}/reduce-stock")
    ProductDTO reduceStock(@PathVariable("productId") Long productId, @RequestBody ReduceStockDTO body);

    @PutMapping("/api/products/{productId}/restock")
    ProductDTO restock(@PathVariable("productId") Long productId, @RequestBody ReduceStockDTO body);
}
