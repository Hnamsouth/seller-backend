package com.vtp.vipo.seller.common.dto.request;

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
public class TokenEvtpRequest {
    @JsonProperty("USERNAME")
    String username;

    @JsonProperty("PASSWORD")
    String password;
}
