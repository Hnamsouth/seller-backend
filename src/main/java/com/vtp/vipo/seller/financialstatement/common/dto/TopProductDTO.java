package com.vtp.vipo.seller.financialstatement.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class TopProductDTO {

    @Builder.Default
    String sequenceNum = "";

    String productName;

    Integer quantity;

    String originalPrice;

    String negotiatedPrice;

    String finalPrice;

    String revenue;

    @Builder.Default
    String revenuePercentage= "";

}
