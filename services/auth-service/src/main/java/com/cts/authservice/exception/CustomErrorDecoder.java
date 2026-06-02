package com.cts.authservice.exception;

import com.cts.authservice.exception.custom.DownstreamException;
import com.cts.authservice.exception.custom.ServiceUnavailableException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

/**
 * Feign error decoder that maps downstream HTTP error statuses to
 * domain-specific exceptions.
 */
@Slf4j
public class CustomErrorDecoder implements ErrorDecoder {
    /**
     * Decodes a downstream error response into the appropriate exception.
     *
     * @param methodKey the Feign method that triggered the call
     * @param response  the downstream HTTP response
     * @return never returns normally; always throws a mapped exception
     */
    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());
        log.warn("Decoding downstream error for {}: status={}", methodKey, status);

        if(status == HttpStatus.SERVICE_UNAVAILABLE || status == HttpStatus.GATEWAY_TIMEOUT) {
            throw new ServiceUnavailableException("Downstream Service Unavailable: " + methodKey);
        }

        throw new DownstreamException("Downstream error in " + methodKey, status);
    }
}
