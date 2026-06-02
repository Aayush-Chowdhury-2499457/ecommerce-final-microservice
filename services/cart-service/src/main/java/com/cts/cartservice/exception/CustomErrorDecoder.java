package com.cts.cartservice.exception;

import com.cts.cartservice.exception.custom.DownstreamException;
import com.cts.cartservice.exception.custom.ServiceUnavailableException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

/**
 * Translates Feign error responses into typed cart-service exceptions.
 */
@Slf4j
public class CustomErrorDecoder implements ErrorDecoder {
    /** Maps the downstream HTTP status to a {@link ServiceUnavailableException} or {@link DownstreamException}. */
    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());
        log.warn("Decoding downstream error for {}: status={}", methodKey, status.value());

        if(status == HttpStatus.SERVICE_UNAVAILABLE || status == HttpStatus.GATEWAY_TIMEOUT) {
            throw new ServiceUnavailableException("Downstream Service Unavailable: " + methodKey);
        }

        throw new DownstreamException("Downstream error in " + methodKey, status);
    }
}
