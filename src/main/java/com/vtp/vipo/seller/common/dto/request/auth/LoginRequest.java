package com.vtp.vipo.seller.common.dto.request.auth;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Please enter the phone number")
    private String phone;

    @NotBlank(message = "Please enter the password")
    private String password;
}
