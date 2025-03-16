package com.vtp.vipo.seller.common.exception.enums;

import com.vtp.vipo.seller.common.exception.VipoUnAuthorizationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Enumeration representing authentication-related exceptions in the system.
 *
 * <p>This enum is used to encapsulate error codes and error messages associated
 * with authentication failures. Each constant represents a specific authentication
 * error scenario, and it provides a method to create a {@link VipoUnAuthorizationException}
 * for use in exception handling.</p>
 *
 * <p>Usage Example:</p>
 * <pre>
 *     throw AuthenticationException.INVALID_OAUTH2_CREDENTIAL.asException();
 * </pre>
 *
 * <p>Benefits:</p>
 * <ul>
 *     <li>Centralized management of error codes and messages.</li>
 *     <li>Improved readability and maintainability of exception-handling logic.</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum VipoAuthenticationException {

    /**
     * Error when OAuth2 credentials are invalid.
     */
    INVALID_OAUTH2_CREDENTIAL("100", "Invalid OAuth2 credentials"),
    /**
     * Bad request
     */
    BAD_REQUEST("101", "Bad Request");;

    /**
     * The unique error status code for the exception.
     */
    String errorStatus;

    /**
     * The descriptive error message for the exception.
     */
    String errorMessage;

    /**
     * Creates a {@link VipoUnAuthorizationException} instance with the associated
     * error status and message of the enum constant.
     *
     * @return A new {@link VipoUnAuthorizationException} instance.
     */
    public VipoUnAuthorizationException asException() {
        return new VipoUnAuthorizationException(this.errorStatus, this.errorMessage);
    }

}
