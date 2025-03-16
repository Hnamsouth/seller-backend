package com.vtp.vipo.seller.controllers;

import com.vtp.vipo.seller.common.BaseController;
import com.vtp.vipo.seller.common.dto.request.order.report.OrderPackageReportExportRequest;
import com.vtp.vipo.seller.common.dto.response.reportexport.ReportExportDownloadResponse;
import com.vtp.vipo.seller.common.enumseller.OrderFilterTab;
import com.vtp.vipo.seller.common.utils.ResponseUtils;
import com.vtp.vipo.seller.services.order.ReportExportService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

/**
 * For order_package export
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportExportController extends BaseController<ReportExportService> {

    @GetMapping("/histories")
    public ResponseEntity<?> getReportHistory(
            @RequestParam(required = false) String reportName,
            @RequestParam(required = false) Long fromDate,
            @RequestParam(required = false) Long toDate,
            @RequestParam(defaultValue = "0", required = false) int pageNum,
            @RequestParam(defaultValue = "10", required = false) int pageSize
    ){
//        return ResponseUtils.success(service.getReportHistory(reportName, fromDate, toDate, pageNum, pageSize));
        return ResponseUtils.success(service.getReportHistoryV2(reportName, fromDate, toDate, pageNum, pageSize));

    }

    /* Phase 5: UC06 */
    @PostMapping("/export")
    public ResponseEntity<?> requestReportExport(
            @RequestBody OrderPackageReportExportRequest orderPackageReportExportRequest
    ) {
        return ResponseUtils.success(service.requestReportExport(orderPackageReportExportRequest));
    }

    @GetMapping("/status/{report_id}")
    public ResponseEntity<?> checkStatusReport(@PathVariable(name = "report_id") Long reportId){
        return ResponseUtils.success(service.getReportExportById(reportId));
    }


    /**
     * Endpoint to download a report export by its ID.
     *
     * @param reportExportId The ID of the report export.
     * @return The file as a downloadable response.
     */
    @GetMapping("/{reportExportId}/download")
    public ResponseEntity<InputStreamResource> downloadReport(@PathVariable Long reportExportId) {
        ReportExportDownloadResponse downloadResponse = service.downloadFile(reportExportId);
        if (ObjectUtils.isEmpty(downloadResponse))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(downloadResponse.getContentType()));
        headers.setContentDisposition(downloadResponse.getContentDisposition());

        logger.info("Content disposition is {} ", headers.getContentDisposition());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(downloadResponse.getContentLength())
                .body(downloadResponse.getResource());
    }

    @GetMapping("/{reportExportId}")
    public ResponseEntity<?> getReportExportDetail(@PathVariable Long reportExportId) {
        return ResponseUtils.success(service.getReportExportDetail(reportExportId));
    }

}
