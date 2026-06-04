package com.cts.reviewservice.exception;

import com.cts.reviewservice.exception.custom.DownstreamException;
import com.cts.reviewservice.exception.custom.ServiceUnavailableException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

/**
 * Feign error decoder mapping downstream HTTP error statuses to domain exceptions.
 */
@Slf4j
public class CustomErrorDecoder implements ErrorDecoder {
    /** Translates a Feign error response into a {@link ServiceUnavailableException} or {@link DownstreamException}. */
    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());
        log.warn("Decoding downstream error for {} with status {}", methodKey, status);
        if (status == HttpStatus.SERVICE_UNAVAILABLE || status == HttpStatus.GATEWAY_TIMEOUT) {
            throw new ServiceUnavailableException("Downstream Service Unavailable: " + methodKey);
        }
        throw new DownstreamException("Downstream error in " + methodKey, status);
    }
}