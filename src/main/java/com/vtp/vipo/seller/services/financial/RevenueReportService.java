package com.vtp.vipo.seller.services.financial;

import com.vtp.vipo.seller.common.dao.entity.RevenueReportEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.financial.PeriodType;
import com.vtp.vipo.seller.common.dto.response.financial.FinancialReportResponse;
import com.vtp.vipo.seller.common.dto.response.financial.TimeRange;

import java.time.LocalDate;
import java.util.Optional;

public interface RevenueReportService {
    Long save(FinancialReportResponse reportData, Long merchantId, TimeRange timeRange);

    void update(FinancialReportResponse reportData, Long reportId, TimeRange timeRange);

    Optional<RevenueReportEntity> findReportByPeriod(Long merchantId, PeriodType periodType, LocalDate startDate, LocalDate endDate);
}
