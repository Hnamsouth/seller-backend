package com.vtp.vipo.seller.services.order;

import com.vtp.vipo.seller.business.event.kafka.base.OrderPackageReportExportMsg;
import com.vtp.vipo.seller.common.dao.entity.ReportExportEntity;
import com.vtp.vipo.seller.common.dto.request.order.report.OrderPackageReportExportRequest;
import com.vtp.vipo.seller.common.dto.response.ReportHistoryProjection;
import com.vtp.vipo.seller.common.dto.response.base.PagingRs;
import com.vtp.vipo.seller.common.dto.response.order.report.OrderPackageReportExportResponse;
import com.vtp.vipo.seller.common.dto.response.reportexport.ReportExportDownloadResponse;
import com.vtp.vipo.seller.common.enumseller.OrderFilterTab;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import com.vtp.vipo.seller.common.dto.response.reportexport.ReportExportDetailResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for handling order package report export requests.
 * <p>
 * This service provides the method to process the export request for order
 * package reports, filtering by various parameters such as order status, order code,
 * buyer name, product name, shipment code, and creation date range. It generates a response
 * with the appropriate report data based on the criteria specified in the request.
 * </p>
 *
 * <p><strong>Note:</strong> The input request is validated to ensure the integrity of the report generation request.</p>
 */
public interface ReportExportService {

    PagingRs getReportHistory(String reportName, Long fromDate, Long toDate, int pageNum, int pageSize);

    PagingRs getReportHistoryV2(String reportName, Long fromDate, Long toDate, int pageNum, int pageSize);

    ReportHistoryProjection getReportExportById(Long reportId);

    /**
     * Processes the report export request for order packages.
     *
     * @param orderPackageReportExportRequest The request containing filtering criteria for the report.
     *
     */
    OrderPackageReportExportResponse requestReportExport(
            @NotNull @Valid OrderPackageReportExportRequest orderPackageReportExportRequest
    );

    void exportOrderPackageReport(OrderPackageReportExportMsg content) throws IOException;

    ReportExportDownloadResponse downloadFile(@NotNull Long reportExportId);

    ReportExportDetailResponse getReportExportDetail(Long reportExportId);

    void deleteOldReportOccasionally();

    void markExportReportAsFailed(OrderPackageReportExportMsg content, String errorMsg);
}
