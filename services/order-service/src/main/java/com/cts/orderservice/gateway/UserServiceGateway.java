package com.cts.orderservice.gateway;

import com.cts.orderservice.client.UserServiceClient;
import com.cts.orderservice.dto.external.AddressDTO;
import com.cts.orderservice.exception.custom.DownstreamException;
import com.cts.orderservice.exception.custom.ResourceNotFoundException;
import com.cts.orderservice.exception.custom.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceGateway {

    private static final String USER_SERVICE_CB = "userService";

    private final UserServiceClient userServiceClient;

    @Retry(name = USER_SERVICE_CB)
    @CircuitBreaker(name = USER_SERVICE_CB, fallbackMethod = "getAddressFallback")
    public AddressDTO getAddress(Long userId, Long addressId) {
        return userServiceClient.getAddressById(userId, addressId);
    }

    public AddressDTO getAddressFallback(Long userId, Long addressId, Throwable ex) {
        if (ex instanceof DownstreamException de && de.getStatusCode() == 404) {
            throw new ResourceNotFoundException(
                    "Address not found for userId=" + userId + ", addressId=" + addressId);
        }
        log.error("User Service Fallback (getAddress): {}", ex.getMessage());
        throw new ServiceUnavailableException("User Service Unavailable, please try again later");
    }
}
