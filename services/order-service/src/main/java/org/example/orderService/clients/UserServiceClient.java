package org.example.orderService.clients;

import org.example.orderService.dtos.external.AddressDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @GetMapping("/api/users/{userId}/addresses/{addressId}")
    AddressDto getAddressById(
            @PathVariable("userId") Long userId,
            @PathVariable("addressId") Long addressId
    );
}