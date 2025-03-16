package com.vtp.vipo.seller.common.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class ReportRequest {

    @JsonProperty("UserName")
    @NotBlank(message = "Please enter the userName number")
    private String userName;

    @JsonProperty("PassWord")
    @NotBlank(message = "Please enter the password")
    private String passWord;
}
