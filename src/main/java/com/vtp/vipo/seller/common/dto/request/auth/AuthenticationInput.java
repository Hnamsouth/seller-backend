package com.vtp.vipo.seller.common.dto.request.auth;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.enumseller.AuthenticationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * @author haidv
 * @version 1.0
 */
@Setter
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AuthenticationInput {

    @NotNull
    private AuthenticationType authenticationType;

    @Schema(description = "The unique identifier of the user (e.g., username, email, phone number).")
    private String identifier;

    @Schema(description = "The credential used for authentication (e.g., password, OTP, or token).")
    private String credential;

}

