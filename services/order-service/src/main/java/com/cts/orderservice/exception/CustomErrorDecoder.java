package com.cts.orderservice.exception;

import com.cts.orderservice.exception.custom.DownstreamException;
import com.cts.orderservice.exception.custom.ServiceUnavailableException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

/**
 * Feign {@link ErrorDecoder} that maps downstream HTTP errors to domain exceptions:
 * 503/504 become {@link ServiceUnavailableException}; all others become
 * {@link DownstreamException} carrying the original status code.
 */
@Slf4j
public class CustomErrorDecoder implements ErrorDecoder {
    /**
     * Translates a downstream Feign response into the appropriate domain exception.
     *
     * @param methodKey the Feign method that failed
     * @param response  the downstream response
     * @return the exception to propagate
     */
    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());
        log.warn("Downstream error in {}: status={}", methodKey, status.value());
        if (status == HttpStatus.SERVICE_UNAVAILABLE || status == HttpStatus.GATEWAY_TIMEOUT) {
            throw new ServiceUnavailableException("Downstream Service Unavailable: " + methodKey);
        }
        throw new DownstreamException("Downstream error in " + methodKey, status);
    }
}