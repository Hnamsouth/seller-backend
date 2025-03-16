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
public class CreateOrderEvtpResponse {
    @JsonProperty("status")
    int status;

    @JsonProperty("error")
    boolean error;

    @JsonProperty("message")
    String message;

    @JsonProperty("data")
    OrderData data;

    @ToString
    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OrderData {
        @JsonProperty("ORDER_NUMBER")
        String orderNumber; // Mã vận đơn(Do Viettelpost tự sinh)

        @JsonProperty("MONEY_COLLECTION")
        Long moneyCollection; // Tổng tiền thu hộ

        @JsonProperty("EXCHANGE_WEIGHT")
        Long exchangeWeight; // Trọng lượng quy đổi

        @JsonProperty("MONEY_TOTAL")
        Long moneyTotal; // Tổng cước phí

        @JsonProperty("MONEY_TOTAL_FEE")
        Long moneyTotalFee; // Phí vận chuyển

        @JsonProperty("MONEY_FEE")
        Long moneyFee; // Phí xăng dầu

        @JsonProperty("MONEY_COLLECTION_FEE")
        Long moneyCollectionFee; // Phí thu hộ

        @JsonProperty("MONEY_OTHER_FEE")
        Long moneyOtherFee; // Phí khác

        @JsonProperty("MONEY_VAS")
        Long moneyVas; // Phí dịch vụ

        @JsonProperty("MONEY_VAT")
        Long moneyVat; // Thuế giá trị gia tăng

        @JsonProperty("KPI_HT")
        Double kpiHt; // Thời gian giao hàng cam kết(Tính từ 24 giờ ngày nhận được đơn hàng).

        @JsonProperty("RECEIVER_PROVINCE")
        Long receiverProvince; // ID Tỉnh nhận

        @JsonProperty("RECEIVER_DISTRICT")
        Long receiverDistrict; // ID Quận nhận

        @JsonProperty("RECEIVER_WARDS")
        Long receiverWards; // ID Phường nhận
    }
}
