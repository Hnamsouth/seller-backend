package com.vtp.vipo.seller.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.enumseller.PriceAdjustmentType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PriceAdjustmentTotalResponse {
    Long id;

    Long orderId;

    PriceAdjustmentType type;

    String createdBy;

    LocalDateTime createdAt;

    BigDecimal adjustedAmount;

    BigDecimal priceBeforeAdjustment;

    BigDecimal priceAfterAdjustment;

    String refCode;
}
