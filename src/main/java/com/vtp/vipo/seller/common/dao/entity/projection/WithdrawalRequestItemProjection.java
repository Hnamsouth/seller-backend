package com.vtp.vipo.seller.common.dao.entity.projection;

import java.math.BigDecimal;

public interface WithdrawalRequestItemProjection {

    Integer getStt();

    Long getOrderPackageId();

    String getOrderPackageCode();

    Long getSuccessDeliveryDate();

    Long getWithdrawalTime();

    String getShippingCode();

    String getBuyerName();

    Long getQuantity();

    String getPrepaymentTransactionCode();

    BigDecimal getPrepayment();

    BigDecimal getCodAmount();

    BigDecimal getOrderAmount();

    String getFees();

    BigDecimal getAdjustmentPrice();

    BigDecimal getEstimatedProfit();

}
