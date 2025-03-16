package com.vtp.vipo.seller.services.financial;

import com.vtp.vipo.seller.common.dto.request.financial.FinancialReportRequest;
import com.vtp.vipo.seller.common.dto.response.base.PagingRs;
import com.vtp.vipo.seller.common.dto.response.financial.FinancialReportResponse;
import com.vtp.vipo.seller.financialstatement.common.dto.request.RevenueReportExportInfoRequest;
import com.vtp.vipo.seller.financialstatement.common.dto.request.RevenueReportExportMsg;
import com.vtp.vipo.seller.financialstatement.common.dto.response.RevenueReportExportInfoResponse;

public interface FinancialService {
    PagingRs filterProduct(Integer page, Integer pageSize, String keyword);

    FinancialReportResponse exportReportRevenue(FinancialReportRequest request);

    RevenueReportExportInfoResponse getExportRevenueReport(RevenueReportExportInfoRequest exportInfoRequest);

    RevenueReportExportInfoResponse getExportRevenueReportReal(RevenueReportExportInfoRequest exportInfoRequest);

    void exportRevenueReport(RevenueReportExportMsg content);
}
