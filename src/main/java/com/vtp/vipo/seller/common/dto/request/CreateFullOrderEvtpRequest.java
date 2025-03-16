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
public class CreateFullOrderEvtpRequest {

    @JsonProperty("ORDER_NUMBER")
    String orderNumber;

    @JsonProperty("GROUPADDRESS_ID")
    Long groupAddressId;

    @JsonProperty("SENDER_FULLNAME")
    String senderFullname;

    @JsonProperty("SENDER_PHONE")
    String senderPhone;

    @JsonProperty("SENDER_ADDRESS")
    String senderAddress;

    @JsonProperty("SENDER_PROVINCE")
    Long senderProvince;

    @JsonProperty("SENDER_DISTRICT")
    Long senderDistrict;

    @JsonProperty("SENDER_WARDS")
    Long senderWards;

    @JsonProperty("RECEIVER_FULLNAME")
    String receiverFullname;

    @JsonProperty("RECEIVER_PHONE")
    String receiverPhone;

    @JsonProperty("RECEIVER_ADDRESS")
    String receiverAddress;

    @JsonProperty("RECEIVER_PROVINCE")
    Long receiverProvince;

    @JsonProperty("RECEIVER_DISTRICT")
    Long receiverDistrict;

    @JsonProperty("RECEIVER_WARDS")
    Long receiverWards;

    @JsonProperty("PRODUCT_NAME")
    String productName;

    @JsonProperty("PRODUCT_DESCRIPTION")
    String productDescription;

    @JsonProperty("PRODUCT_QUANTITY")
    Long productQuantity;

    @JsonProperty("PRODUCT_PRICE")
    Long productPrice;

    @JsonProperty("PRODUCT_WEIGHT")
    Long productWeight;

    @JsonProperty("PRODUCT_LENGTH")
    Long productLength;

    @JsonProperty("PRODUCT_WIDTH")
    Long productWidth;

    @JsonProperty("PRODUCT_HEIGHT")
    Long productHeight;

    @JsonProperty("ORDER_PAYMENT")
    Long orderPayment;

    @JsonProperty("ORDER_SERVICE")
    String orderService;

    @JsonProperty("ORDER_SERVICE_ADD")
    String orderServiceAdd;

    @JsonProperty("ORDER_NOTE")
    String orderNote;

    @JsonProperty("MONEY_COLLECTION")
    Long moneyCollection;

    @JsonProperty("CHECK_UNIQUE")
    boolean checkUnique;

    @JsonProperty("EXTRA_MONEY")
    Long extraMoney;

    @JsonProperty("PRODUCT_TYPE")
    String productType;

    @JsonProperty("LIST_ITEM")
    List<ListItem> listItem;

    @ToString
    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ListItem {
        @JsonProperty("PRODUCT_NAME")
        String productName;

        @JsonProperty("PRODUCT_PRICE")
        Long productPrice;

        @JsonProperty("PRODUCT_WEIGHT")
        Long productWeight;

        @JsonProperty("PRODUCT_QUANTITY")
        Long productQuantity;
    }
}
