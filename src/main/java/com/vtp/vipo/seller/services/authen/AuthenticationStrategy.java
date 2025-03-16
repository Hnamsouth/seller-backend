package com.vtp.vipo.seller.services.authen;

import com.vtp.vipo.seller.common.dao.entity.MerchantEntity;
import org.springframework.validation.annotation.Validated;

/**
 * Interface defining the authentication strategy for various authentication providers.
 * <p>
 * This interface supports both direct user authentication and linking external OAuth2
 * accounts to existing user accounts in the system.
 * </p>
 *
 * @author haidv
 * @version 1.0
 */
@Validated
public interface AuthenticationStrategy {

    /**
     * Authenticates a user based on the provided identifier and credential.
     * <p>
     * Implementations of this method should validate the credentials against the
     * specific authentication provider (e.g., database, OAuth2 provider) and
     * return the authenticated {@link MerchantEntity}.
     * </p>
     *
     * @param identifier The unique identifier for the user (e.g., username, email).
     * @param credential The credential for authentication (e.g., password, OAuth2 token).
     * @return The authenticated {@link MerchantEntity} object.
     * @throws IllegalArgumentException If the authentication fails or is invalid.
     */
    MerchantEntity authenticate(String identifier, String credential);

}
