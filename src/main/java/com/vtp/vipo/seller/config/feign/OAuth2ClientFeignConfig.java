package com.vtp.vipo.seller.config.feign;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Feign clients interacting with OAuth2 authentication servers.
 * <p>
 * This configuration provides a custom {@link ErrorDecoder} to handle specific
 * HTTP response codes and map them to meaningful exceptions.
 * </p>
 */
@Configuration
public class OAuth2ClientFeignConfig {

    /**
     * Configures a custom {@link ErrorDecoder} for Feign clients.
     * <p>
     * The {@link OAuth2ErrorDecoder} is used to translate HTTP status codes
     * from responses into application-specific exceptions, allowing consistent
     * error handling across Feign clients.
     * </p>
     *
     * @return A custom {@link ErrorDecoder} instance.
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new OAuth2ErrorDecoder();
    }
}
