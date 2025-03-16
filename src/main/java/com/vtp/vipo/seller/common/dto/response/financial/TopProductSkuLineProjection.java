package com.vtp.vipo.seller.common.dto.response.financial;

import java.math.BigDecimal;

public interface TopProductSkuLineProjection {
    Long getPackageId();
    BigDecimal getOpNegotiatedAmount(); // discount toàn đơn

    Long getProductId();
    Long getSkuId();

    BigDecimal getPrice();
    Integer getQuantity();
    BigDecimal getPpNegotiatedAmount();

    String getLabel(); // chuỗi "Loại: Hương nhài, Size: 100ml" v.v.

    default String show() {
        return "TopProductSkuLineProjection{" +
                "packageId=" + getPackageId() +
                ", opNegotiatedAmount=" + getOpNegotiatedAmount() +
                ", productId=" + getProductId() +
                ", skuId=" + getSkuId() +
                ", price=" + getPrice() +
                ", quantity=" + getQuantity() +
                ", ppNegotiatedAmount=" + getPpNegotiatedAmount() +
                ", label=" + getLabel() +
                '}';
    }
}