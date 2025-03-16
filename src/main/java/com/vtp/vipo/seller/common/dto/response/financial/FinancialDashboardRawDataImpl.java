package com.vtp.vipo.seller.common.dto.response.financial;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FinancialDashboardRawDataImpl implements FinancialDashboardRawData {
    long totalOrders;
    long totalBuyers;
    long convertedOrders;
    long deliveredOrders;
    long returnCancelOrders;

    BigDecimal revenue;
    BigDecimal platformFee;
    BigDecimal otherFee;
    BigDecimal priceNegotiated;

    public FinancialDashboardRawDataImpl() {
        this.totalOrders = 0;
        this.totalBuyers = 0;
        this.convertedOrders = 0;
        this.deliveredOrders = 0;
        this.returnCancelOrders = 0;
        this.revenue = BigDecimal.ZERO;
        this.platformFee = BigDecimal.ZERO;
        this.otherFee = BigDecimal.ZERO;
        this.priceNegotiated = BigDecimal.ZERO;
    }

    @Override
    public long getTotalOrders() {
        return totalOrders;
    }

    @Override
    public long getTotalBuyers() {
        return totalBuyers;
    }

    @Override
    public long getConvertedOrders() {
        return convertedOrders;
    }

    @Override
    public long getDeliveredOrders() {
        return deliveredOrders;
    }

    @Override
    public long getReturnCancelOrders() {
        return returnCancelOrders;
    }

    @Override
    public BigDecimal getRevenue() {
        return revenue;
    }

    @Override
    public BigDecimal getPlatformFee() {
        return platformFee;
    }

    @Override
    public BigDecimal getOtherFee() {
        return otherFee;
    }

    @Override
    public BigDecimal getPriceNegotiated() {
        return priceNegotiated;
    }
}
