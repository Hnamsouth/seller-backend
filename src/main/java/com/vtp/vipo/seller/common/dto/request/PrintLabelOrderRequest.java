package com.vtp.vipo.seller.common.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrintLabelOrderRequest {
    @JsonProperty("ORDER_ARRAY")
    List<String> orderArray;
}
