package com.vtp.vipo.seller.common.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenEvtpResponse {
    @JsonProperty("status")
    int status;

    @JsonProperty("error")
    boolean error;

    @JsonProperty("message")
    String message;

    @JsonProperty("data")
    TokenData data;

    @ToString
    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TokenData {

        @JsonProperty("userId")
        long userId;

        @JsonProperty("token")
        String token;

        @JsonProperty("partner")
        long partner;

        @JsonProperty("phone")
        String phone;

        @JsonProperty("postcode")
        String postcode;

        @JsonProperty("expired")
        long expired;

        @JsonProperty("encrypted")
        String encrypted;

        @JsonProperty("source")
        long source;

        @JsonProperty("infoUpdated")
        boolean infoUpdated;
    }
}
