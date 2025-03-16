package com.vtp.vipo.seller.common.dto;

import com.vtp.vipo.seller.common.enumseller.PriceAdjustmentType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PriceAdjustmentSkuData extends ActivityDetailsData {
    PriceAdjustmentType type;

    String refCode;

    List<SkuDetails> skus;

    @ToString
    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SkuDetails {
        String skuId;

        BigDecimal priceBeforeAdjustment;

        BigDecimal priceAfterAdjustment;

        Long quantity;
    }
}
