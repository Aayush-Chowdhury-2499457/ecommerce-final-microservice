package com.cts.cartservice.config;

import com.cts.cartservice.exception.CustomErrorDecoder;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

/**
 * Feign configuration registering the custom error decoder for Feign clients.
 */
@Slf4j
public class FeignConfig {

    /** Provides the {@link CustomErrorDecoder} used to translate Feign errors. */
    @Bean
    public ErrorDecoder errorDecoder() {
        log.debug("Registering CustomErrorDecoder for Feign clients");
        return new CustomErrorDecoder();
    }
}
