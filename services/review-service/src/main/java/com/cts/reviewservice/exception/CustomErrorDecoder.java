package com.cts.reviewservice.exception;

import com.cts.reviewservice.exception.custom.DownstreamException;
import com.cts.reviewservice.exception.custom.ServiceUnavailableException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

public class CustomErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());
        if (status == HttpStatus.SERVICE_UNAVAILABLE || status == HttpStatus.GATEWAY_TIMEOUT) {
            throw new ServiceUnavailableException("Downstream Service Unavailable: " + methodKey);
        }
        throw new DownstreamException("Downstream error in " + methodKey, status);
    }
}