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
public class CreateSimplifiedOrderEvtpRequest {
    @JsonProperty("ORDER_NUMBER")
    private String orderNumber;

    @JsonProperty("SENDER_FULLNAME")
    private String senderFullname;

    @JsonProperty("SENDER_ADDRESS")
    private String senderAddress;

    @JsonProperty("SENDER_PHONE")
    private String senderPhone;

    @JsonProperty("RECEIVER_FULLNAME")
    private String receiverFullname;

    @JsonProperty("RECEIVER_ADDRESS")
    private String receiverAddress;

    @JsonProperty("RECEIVER_PHONE")
    private String receiverPhone;

    @JsonProperty("PRODUCT_NAME")
    private String productName;

    @JsonProperty("PRODUCT_DESCRIPTION")
    private String productDescription;

    @JsonProperty("PRODUCT_QUANTITY")
    private int productQuantity;

    @JsonProperty("PRODUCT_PRICE")
    private int productPrice;

    @JsonProperty("PRODUCT_WEIGHT")
    private int productWeight;

    @JsonProperty("PRODUCT_LENGTH")
    private int productLength;

    @JsonProperty("PRODUCT_WIDTH")
    private int productWidth;

    @JsonProperty("PRODUCT_HEIGHT")
    private int productHeight;

    @JsonProperty("ORDER_PAYMENT")
    private int orderPayment;

    @JsonProperty("ORDER_SERVICE")
    private String orderService;

    @JsonProperty("PRODUCT_TYPE")
    private String productType;

    @JsonProperty("ORDER_SERVICE_ADD")
    private String orderServiceAdd;

    @JsonProperty("ORDER_NOTE")
    private String orderNote;

    @JsonProperty("MONEY_COLLECTION")
    private int moneyCollection;

    @JsonProperty("EXTRA_MONEY")
    private int extraMoney;

    @JsonProperty("CHECK_UNIQUE")
    private boolean checkUnique;

    @JsonProperty("PRODUCT_DETAIL")
    private List<ProductDetail> productDetail;

    @ToString
    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ProductDetail {

        @JsonProperty("PRODUCT_NAME")
        private String productName;

        @JsonProperty("PRODUCT_QUANTITY")
        private int productQuantity;

        @JsonProperty("PRODUCT_PRICE")
        private int productPrice;

        @JsonProperty("PRODUCT_WEIGHT")
        private int productWeight;
    }
}
