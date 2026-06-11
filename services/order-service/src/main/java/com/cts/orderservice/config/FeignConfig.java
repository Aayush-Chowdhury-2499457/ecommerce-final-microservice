package com.cts.orderservice.config;

import com.cts.orderservice.exception.CustomErrorDecoder;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

/**
 * Per-client Feign configuration that registers the {@link CustomErrorDecoder} so that
 * downstream HTTP errors are translated into domain exceptions.
 */
@Slf4j
public class FeignConfig {
    /**
     * Provides the custom Feign error decoder.
     *
     * @return the {@link CustomErrorDecoder} bean
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }
}