package com.vtp.vipo.seller.common.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.enumseller.PriceAdjustmentType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PriceAdjustmentSkuResponse {
    Long id;

    Long orderId;

    PriceAdjustmentType type;

    String createdBy;

    LocalDateTime createdAt;

    List<SkuDetails> skus;

    String refCode;

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SkuDetails {
        String skuId;

        BigDecimal priceBeforeAdjustment;

        BigDecimal priceAfterAdjustment;

        Long quantity;
    }
}
