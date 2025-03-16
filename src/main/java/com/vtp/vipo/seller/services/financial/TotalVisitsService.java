package com.vtp.vipo.seller.services.financial;

import com.vtp.vipo.seller.common.dto.response.financial.FinancialReportResponse;

public interface TotalVisitsService {
    FinancialReportResponse.TotalVisitsData getTotalVisits(Long merchantId, long currStart, long currEnd);

    FinancialReportResponse.TotalVisitsData getTotalVisitsAllTime(Long merchantId);
}
