package com.vtp.vipo.seller.config.feign;

import com.vtp.vipo.seller.common.exception.VipoFailedToExecuteException;
import com.vtp.vipo.seller.common.exception.enums.VipoAuthenticationException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom Feign {@link ErrorDecoder} for handling responses from OAuth2 authentication servers.
 * <p>
 * This decoder translates HTTP status codes into application-specific exceptions,
 * enabling consistent and meaningful error handling when interacting with third-party
 * OAuth2 providers.
 * </p>
 */
@Slf4j
public class OAuth2ErrorDecoder implements ErrorDecoder {

    /**
     * Decodes Feign client responses and maps HTTP status codes to application-specific exceptions.
     *
     * @param methodKey The Feign client method that triggered the request.
     * @param response  The HTTP response received from the server.
     * @return An appropriate {@link Exception} based on the HTTP status code.
     */
    @Override
    public Exception decode(String methodKey, Response response) {
        // Log error details for debugging and analysis
        log.info("Feign call failed: method={}, status={}, reason={}",
                methodKey, response.status(), response.reason());

        // Map HTTP status codes to specific exceptions
        return switch (response.status()) {
            case 401 -> // Unauthorized
                    VipoAuthenticationException.INVALID_OAUTH2_CREDENTIAL.asException();
            case 400 -> // Bad Request
                    VipoAuthenticationException.BAD_REQUEST.asException();
            case 500 -> // Internal Server Error
                    new VipoFailedToExecuteException("Failed at Authentication Server! Please try again later!");
            default -> // Unexpected HTTP status
                    new VipoFailedToExecuteException("Unexpected error occurred: " + response.status());
        };
    }
}
