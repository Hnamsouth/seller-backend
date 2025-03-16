package com.vtp.vipo.seller.services.authen.impl.strategies;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * Request object for updating a user's information using SSO.
 *
 * <p>This object is serialized and sent to the VTP API when performing the update operation.</p>
 */
@Data
@Builder
public class SsoUpdateUserRequest {

    /**
     * The SSO token for authenticating the request.
     */
    @JsonProperty("TokenSSO")
    private String tokenSSO;

    /**
     * The source identifier of the request.
     */
    @JsonProperty("Source")
    private Integer source;
}