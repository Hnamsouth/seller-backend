package com.vtp.vipo.seller.services.authen;

import com.vtp.vipo.seller.common.dao.entity.MerchantEntity;
import com.vtp.vipo.seller.common.enumseller.AuthenticationType;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * @author haidv
 * @version 1.0
 */
@Validated
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationContext {

    AuthenticationFactory authenticationFactory;

    public MerchantEntity authenticate(String identifier, String credential, AuthenticationType authenticationType) {
        return getStrategy(authenticationType).authenticate(identifier, credential);
    }

    /**
     * Retrieves the appropriate {@link AuthenticationStrategy} for the given authentication type.
     *
     * @param authenticationType The type of authentication provider.
     * @return The {@link AuthenticationStrategy} associated with the authentication type.
     * @throws IllegalArgumentException If no strategy is found for the specified authentication type.
     */
    private AuthenticationStrategy getStrategy(@NotNull AuthenticationType authenticationType) {
        // Fetch the strategy from the authentication factory
        AuthenticationStrategy strategy = authenticationFactory.getStrategy(authenticationType);

        // Validate that the strategy exists
        if (ObjectUtils.isEmpty(strategy)) {
            throw new IllegalArgumentException(
                    "Authentication type not supported: " + authenticationType);
        }

        return strategy;
    }

}
