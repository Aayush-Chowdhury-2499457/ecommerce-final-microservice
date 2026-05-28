package org.example.orderService.clients;

import org.example.orderService.dtos.external.AddressDto;
import org.example.orderService.exceptions.ServiceUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public AddressDto getAddressById(Long userId, Long addressId) {
        throw new ServiceUnavailableException("User service unavailable, cannot place order");
    }
}
