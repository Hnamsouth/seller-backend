package com.vtp.vipo.seller.common.dto;

import com.vtp.vipo.seller.common.enumseller.PriceAdjustmentType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PriceAdjustmentTotalData extends ActivityDetailsData {
    PriceAdjustmentType type;

    BigDecimal adjustedAmount;

    BigDecimal priceBeforeAdjustment;

    BigDecimal priceAfterAdjustment;

    String refCode;
}
