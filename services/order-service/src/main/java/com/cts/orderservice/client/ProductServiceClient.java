package com.cts.orderservice.client;

import com.cts.orderservice.config.FeignConfig;
import com.cts.orderservice.dto.external.ProductDTO;
import com.cts.orderservice.dto.external.ReduceStockDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service", configuration = FeignConfig.class)
public interface ProductServiceClient {

    @GetMapping("/api/products/{productId}")
    ProductDTO getProductById(@PathVariable("productId") Long productId);

    @PatchMapping("/api/products/{productId}/reduce-stock")
    ProductDTO reduceStock(@PathVariable("productId") Long productId, @RequestBody ReduceStockDTO body);
}
