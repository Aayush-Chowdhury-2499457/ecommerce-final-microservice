package com.cts.orderservice.client;

import com.cts.orderservice.config.FeignConfig;
import com.cts.orderservice.dto.external.AddressDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for the user service.
 */
@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface UserServiceClient {

    /** Retrieves a user's delivery address by user id and address id. */
    @GetMapping("/api/users/{userId}/addresses/{addressId}")
    AddressDTO getAddressById(@PathVariable("userId") Long userId,
                              @PathVariable("addressId") Long addressId);
}
