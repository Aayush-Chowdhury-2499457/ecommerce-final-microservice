package com.cts.paymentservice.exception;

import com.cts.paymentservice.exception.custom.DownstreamException;
import com.cts.paymentservice.exception.custom.ServiceUnavailableException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

/**
 * Feign {@link ErrorDecoder} that maps downstream HTTP error responses into domain
 * exceptions: 503/504 to {@link ServiceUnavailableException}, others to
 * {@link DownstreamException}.
 */
@Slf4j
public class CustomErrorDecoder implements ErrorDecoder {
    /**
     * Translates a failed downstream response into the appropriate domain exception.
     *
     * @param methodKey the Feign method that triggered the call
     * @param response  the downstream error response
     * @return never returns normally; always throws
     */
    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());
        log.error("Downstream error from {}: status={}", methodKey, status.value());
        if (status == HttpStatus.SERVICE_UNAVAILABLE || status == HttpStatus.GATEWAY_TIMEOUT) {
            throw new ServiceUnavailableException("Downstream Service Unavailable: " + methodKey);
        }
        throw new DownstreamException("Downstream error in " + methodKey, status);
    }
}