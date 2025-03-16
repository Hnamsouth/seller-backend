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
public class ServiceInfoEvtpRequest {
    @JsonProperty("SENDER_PROVINCE")
    private Long senderProvince;

    @JsonProperty("SENDER_DISTRICT")
    private Long senderDistrict;

    @JsonProperty("RECEIVER_PROVINCE")
    private Long receiverProvince;

    @JsonProperty("RECEIVER_DISTRICT")
    private Long receiverDistrict;

    @JsonProperty("PRODUCT_TYPE")
    private String productType;

    @JsonProperty("PRODUCT_WEIGHT")
    private Long productWeight;

    @JsonProperty("PRODUCT_PRICE")
    private Long productPrice;

    @JsonProperty("MONEY_COLLECTION")
    private Long moneyCollection;

    @JsonProperty("TYPE")
    private Long type;

    @JsonProperty("PRODUCT_LENGTH")
    private Long productLength;

    @JsonProperty("PRODUCT_WIDTH")
    private Long productWidth;

    @JsonProperty("PRODUCT_HEIGHT")
    private Long productHeight;
}
