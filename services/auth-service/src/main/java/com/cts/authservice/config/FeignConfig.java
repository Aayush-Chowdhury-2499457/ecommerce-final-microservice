package com.cts.authservice.config;

import com.cts.authservice.exception.CustomErrorDecoder;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

/**
 * Feign configuration that registers the custom error decoder
 * used to translate downstream HTTP errors into domain exceptions.
 */
@Slf4j
public class FeignConfig {

    /**
     * Provides the {@link ErrorDecoder} bean for Feign clients.
     *
     * @return a {@link CustomErrorDecoder} instance
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        log.debug("Registering CustomErrorDecoder for Feign clients");
        return new CustomErrorDecoder();
    }
}
