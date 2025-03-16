package com.vtp.vipo.seller.common.dto.response.financial;

import java.math.BigDecimal;

public interface TopProductProjection {
    Long getProductId();

    String getProductName();

    String getProductCode();

    int getQuantitySold();

    BigDecimal getRevenueBeforeNegotiation();

    BigDecimal getRevenueAfterNegotiation();

    default String show() {
        return "TopProductProjection{" +
                "productId=" + getProductId() +
                ", productName='" + getProductName() + '\'' +
                ", productCode='" + getProductCode() + '\'' +
                ", quantitySold=" + getQuantitySold() +
                ", revenueBeforeNegotiation=" + getRevenueBeforeNegotiation() +
                ", revenueAfterNegotiation=" + getRevenueAfterNegotiation() +
                '}';
    }
}
