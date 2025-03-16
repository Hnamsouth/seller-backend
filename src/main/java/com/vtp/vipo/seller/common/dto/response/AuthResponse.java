package com.vtp.vipo.seller.common.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.constants.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@AllArgsConstructor
@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AuthResponse {

    private String accessToken;

    private String type = Constants.BEARER_TOKEN_TYPE;

    private String refreshToken;

    private String phone;

    private Long id;

    public AuthResponse(String accessToken, String refreshToken, String phone) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.phone = phone;
    }

    public AuthResponse(String accessToken, String refreshToken, Long id) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
    }

}
