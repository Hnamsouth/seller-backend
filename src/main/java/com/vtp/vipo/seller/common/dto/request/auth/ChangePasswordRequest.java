package com.vtp.vipo.seller.common.dto.request.auth;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Size;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChangePasswordRequest {

    @Size(min = 6, max = 25, message = "The password must be between 6 and 25 characters.")
    private String currentPassword;

    @Size(min = 6, max = 25, message = "The password new must be between 6 and 25 characters.")
    private String newPassword;

}
