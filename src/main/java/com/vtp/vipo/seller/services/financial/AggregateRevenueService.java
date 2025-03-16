package com.vtp.vipo.seller.services.financial;

import com.vtp.vipo.seller.common.dto.request.financial.FinancialReportRequest;
import com.vtp.vipo.seller.common.dto.response.financial.FinancialReportResponse;
import com.vtp.vipo.seller.common.dto.response.financial.TimeRange;

public interface AggregateRevenueService {
    FinancialReportResponse aggregateRevenueData(FinancialReportRequest request, Long merchantId, TimeRange timeRange);
}
