package com.vtp.vipo.seller.common.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderStatus;
import com.vtp.vipo.seller.common.dto.response.order.LogisticsTrackInfoVO;
import com.vtp.vipo.seller.common.dto.response.order.search.SpecResponse;
import com.vtp.vipo.seller.common.enumseller.FeeType;
import com.vtp.vipo.seller.common.enumseller.OrderAction;
import com.vtp.vipo.seller.common.enumseller.OrderExtraServiceType;
import com.vtp.vipo.seller.common.enumseller.PriceAdjustmentType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderDetailsResponse {
    Long orderId;
    String orderStatus;
    SellerOrderStatus sellerOrderStatus;
    String sellerOrderStatusDesc;
    String orderCode;
    CustomerInfo customerInfo;
    long createdAt;
    List<PackageProduct> packageProducts;
    Integer totalProduct;
    String notes;
    List<ExtraServiceInfo> extraServiceDomesticInfo;
    List<ExtraServiceInfo> extraServiceInternationalInfo;
    PaymentInfo paymentInfo;
    Revenue revenue;
    BigDecimal totalProductPrice;
    BigDecimal totalShippingFee;
    BigDecimal totalPrice;
    BigDecimal totalPriceAdjustment;
    List<PriceAdjustmentHistory> priceAdjustmentHistory;
//    List<LogisticsTrackInfo> logisticsTrackInfo;
    List<LogisticsTrackInfoVO> logisticsTrackInfo;
    ShipmentInfo shipmentInfo;
    List<OrderAction> actions;
    String refCode;

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CustomerInfo {
        String name;
        String phone;
        String fullAddress;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PackageProduct {
        Long id;
        String skuId;
        String productId;
        Long merchantId;
        String sellerOpenId;
        String image;
        String name;
        List<SpecResponse> spec;
        BigDecimal unitPrice;
        BigDecimal unitPriceBeforeAdjustment;
        Long quantity;
        BigDecimal totalPrice;
        BigDecimal totalPriceBeforeAdjustment;
        Boolean isOutOfStock;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ExtraServiceInfo {
        Long extraServiceFeeId;
        Long extraServiceId;
        String name;
        BigDecimal fee;
        String description;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Revenue {
        BigDecimal totalProductPrice;
        List<PlatformDetail> platformFeeMap;
        BigDecimal negotiatedAmount;
        BigDecimal estimatedRevenue;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PriceAdjustmentHistory {
        String id;
        Long orderId;
        PriceAdjustmentType type;
        String createdBy;
        long createdAt;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class LogisticsTrackInfo {
        String context;
        String status;
        String statusDesc;
        long time;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PaymentInfo {
        BigDecimal prepayment;
        BigDecimal paidAmount;
        Long paymentTime;
        Boolean isPaid;
        String paymentMethod;
        String paymentMessage;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ShipmentInfo {
        String carrierName;
        String shipmentCode;
        String shipmentMessage;
        WarehouseAddressInfo pickupAddress;
        List<PackageSplit> packageSplits;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PackageSplit {
        String logisticCode;
        String logisticNo;
        String logisticStatus;
        String description;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class WarehouseAddressInfo {
        Long id;
        String fullAddress;
        String name;
        String phone;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PlatformDetail {
        BigDecimal amount;
        String desc;
        String name;
    }

}
