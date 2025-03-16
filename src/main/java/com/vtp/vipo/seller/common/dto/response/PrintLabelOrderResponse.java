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
public class PrintLabelOrderResponse {
    @JsonProperty("status")
    int status;

    @JsonProperty("error")
    boolean error;

    @JsonProperty("message")
    String message;

    @JsonProperty("data")
    Object data;
}
