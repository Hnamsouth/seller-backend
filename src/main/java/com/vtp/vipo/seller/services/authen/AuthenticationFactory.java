package com.vtp.vipo.seller.services.authen;

import com.vtp.vipo.seller.common.enumseller.AuthenticationType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Factory class for providing the appropriate {@link AuthenticationStrategy} implementation
 * based on the {@link AuthenticationType}.
 *
 * <p>This factory uses the Spring {@link ApplicationContext} to dynamically resolve the required
 * authentication strategy bean by its name. Each strategy bean must be registered with a name
 * matching the {@link AuthenticationType#name()}.</p>
 *
 * <p>Example:</p>
 * <pre>
 * // Assuming an authentication type of "VTP_SSO"
 * AuthenticationStrategy strategy = authenticationFactory.getStrategy(AuthenticationType.VTP_SSO);
 * strategy.authenticate(userInput);
 * </pre>
 *
 * @author haidv
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class AuthenticationFactory {

    private final ApplicationContext applicationContext;

    /**
     * Returns the {@link AuthenticationStrategy} implementation corresponding to the given
     * {@link AuthenticationType}.
     *
     * <p>The bean name must exactly match the {@link AuthenticationType#name()}.</p>
     *
     * @param authenticationType the type of authentication
     * @return the corresponding {@link AuthenticationStrategy} implementation.
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
     *         if no matching bean is found for the given {@code authenticationType}.
     */
    public AuthenticationStrategy getStrategy(AuthenticationType authenticationType) {
        // Fetch and return the bean from the application context based on the authentication type name
        return (AuthenticationStrategy) applicationContext.getBean(authenticationType.name());
    }
}