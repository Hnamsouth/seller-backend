package com.vtp.vipo.seller.common.enumseller;

/**
 * Enum representing the supported authentication types in the application.
 *
 * <p>This enum is used to determine the appropriate authentication strategy for user login.
 * Each authentication type corresponds to a specific implementation of the {@code AuthenticationStrategy} interface.</p>
 *
 * <p>Currently Supported Authentication Types:</p>
 * <ul>
 *   <li>{@link #VTP_SSO}: Authentication using VTP's Single Sign-On system.</li>
 * </ul>
 *
 * <p>Example Usage:</p>
 * <pre>
 * AuthenticationType type = AuthenticationType.VTP_SSO;
 * AuthenticationStrategy strategy = authenticationFactory.getStrategy(type);
 * </pre>
 *
 * @see com.vtp.vipo.seller.services.authen.AuthenticationStrategy
 * @see com.vtp.vipo.seller.services.authen.AuthenticationFactory
 * @version 1.0
 */
public enum AuthenticationType {

    /**
     * VTP Single Sign-On authentication type.
     * <p>This type uses VTP's SSO service for user authentication.</p>
     */
    VTP_SSO;

}