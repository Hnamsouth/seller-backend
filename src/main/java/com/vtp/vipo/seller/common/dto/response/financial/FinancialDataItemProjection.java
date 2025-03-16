package com.vtp.vipo.seller.common.dto.response.financial;

import java.math.BigDecimal;

public interface FinancialDataItemProjection {
    // Tên thời gian
    String getPeriodName();

    // Tổng doanh thu
    BigDecimal getTotalRevenue();

    // Tổng phí
    BigDecimal getTotalCost();

    // Tổng lợi nhuận
    BigDecimal getTotalProfit();
}
