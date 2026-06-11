package com.cts.paymentservice.config;

import com.cts.paymentservice.exception.CustomErrorDecoder;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

/**
 * Feign client configuration that registers the {@link CustomErrorDecoder} for
 * translating downstream HTTP errors into domain exceptions.
 */
@Slf4j
public class FeignConfig {
    /**
     * Provides the custom error decoder for Feign clients using this configuration.
     *
     * @return the configured {@link ErrorDecoder}
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }
}