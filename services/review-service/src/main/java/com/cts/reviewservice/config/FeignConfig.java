package com.cts.reviewservice.config;

import com.cts.reviewservice.exception.CustomErrorDecoder;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

/**
 * Feign client configuration registering the custom error decoder.
 */
@Slf4j
public class FeignConfig {
    /** Provides the {@link CustomErrorDecoder} bean for Feign clients. */
    @Bean
    public ErrorDecoder errorDecoder() {
        log.debug("Registering CustomErrorDecoder for Feign clients");
        return new CustomErrorDecoder();
    }
}