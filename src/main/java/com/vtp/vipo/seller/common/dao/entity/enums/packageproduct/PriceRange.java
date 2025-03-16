package com.vtp.vipo.seller.common.dao.entity.enums.packageproduct;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PriceRange {

    @NotNull
    private BigDecimal price;

    private Integer productPriceType;

    private Integer startQuantity;

    @NotNull
    private BigDecimal systemCurrencyPrice;

}
