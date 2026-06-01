package com.cts.cartservice.client;

import com.cts.cartservice.config.FeignConfig;
import com.cts.cartservice.dto.response.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", configuration = FeignConfig.class)
public interface ProductServiceClient {

    @GetMapping("/api/products/{productId}")
    ResponseEntity<ProductDTO> getProductById(@PathVariable Long productId);
}
