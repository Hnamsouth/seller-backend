package com.vtp.vipo.seller.business.feign;

import com.vtp.vipo.seller.config.feign.OAuth2ClientFeignConfig;
import com.vtp.vipo.seller.services.authen.impl.strategies.SsoUpdateUserRequest;
import com.vtp.vipo.seller.services.authen.impl.strategies.SsoUpdateUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for interacting with the VTP API.
 *
 * <p>This client provides methods for calling external services related to user updates.
 * It uses Spring Cloud OpenFeign for declarative REST client support.</p>
 *
 * <p>Configured to communicate with the VTP API server defined by the
 * property {@code custom.properties.vtpapi-client-url}.</p>
 */
@FeignClient(
        value = "vtpapi-client",
        url = "${custom.properties.vtpapi-client-url}",
        configuration = OAuth2ClientFeignConfig.class
)
public interface VtpApiClient {

    /**
     * Updates a user's information in the VTP system using SSO.
     *
     * @param request the {@link SsoUpdateUserRequest} containing the user's updated details.
     * @return the {@link SsoUpdateUserResponse} containing the result of the update operation.
     */
    @PostMapping("/api/user/ssoUpdateUser")
    SsoUpdateUserResponse updateUser(@RequestBody SsoUpdateUserRequest request);
}

