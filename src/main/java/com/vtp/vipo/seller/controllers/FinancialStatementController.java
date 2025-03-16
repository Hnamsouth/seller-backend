package com.vtp.vipo.seller.controllers;

import com.vtp.vipo.seller.common.BaseController;
import com.vtp.vipo.seller.common.dto.request.financial.FinancialReportRequest;
import com.vtp.vipo.seller.common.utils.ResponseUtils;
import com.vtp.vipo.seller.financialstatement.common.dto.request.RevenueReportExportInfoRequest;
import com.vtp.vipo.seller.financialstatement.common.dto.request.RevenueReportExportMsg;
import com.vtp.vipo.seller.financialstatement.common.enums.FinancialReportFilterType;
import com.vtp.vipo.seller.financialstatement.export.TransformingJRResultSetDataSource;
import com.vtp.vipo.seller.services.financial.FinancialService;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRSwapFile;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/financial")
public class FinancialStatementController extends BaseController<FinancialService> {
    @GetMapping("/product/report-filter")
    public ResponseEntity<?> filterProduct(@RequestParam(required = false, defaultValue = "1") Integer page,
                                           @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                           @RequestParam(required = false) String keyword) {
        return ResponseUtils.success(service.filterProduct(page, pageSize, keyword));
    }

    @PostMapping("/revenue/report")
    public ResponseEntity<?> exportReportRevenue(@RequestBody FinancialReportRequest request) {
        return ResponseUtils.success(service.exportReportRevenue(request));
    }

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/test")
    public ResponseEntity<?> testExport() {
        try {
            InputStream reportStream = getClass().getResourceAsStream("/financial_report.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport("src/main/resources/templates/financialreport.jrxml");
            ResultSet resultSet = jdbcTemplate.getDataSource().getConnection().createStatement(
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY
            ).executeQuery("""
                    SELECT
                        @row_number := @row_number + 1 AS stt,
                        op.deliverySuccessTime AS time,
                        op.orderCode AS orderCode,
                        op.price AS revenue,
                        op.deliverySuccessTime + wc.withdrawAfterSecond AS status
                    FROM order_package op
                             JOIN (SELECT @row_number := 0) init
                        left join merchant m on op.merchantId = m.id
                        left join merchant_group mg on m.merchantGroupId = mg.id
                        left join withdrawal_config wc on mg.id = wc.merchantGroupId
                    WHERE op.sellerOrderStatus = 'ORDER_COMPLETED'
                    ORDER BY op.id desc
                    limit 3000
                    """);

            // Enable MySQL streaming
            jdbcTemplate.getDataSource().getConnection().setAutoCommit(false);

            // 4. Create JRDataSource from ResultSet
            JRDataSource dataSource = new TransformingJRResultSetDataSource(resultSet);

            // 2. Prepare report parameters (e.g., for charts)
            Map<String, Object> parameters = new HashMap<>();
            // 3. Configure a JRSwapFileVirtualizer
            //    - The JRSwapFile is where Jasper will store pages on disk
            //    - The JRSwapFileVirtualizer manages how many pages stay in memory at once (pageCache = 50 in this example)
            JRSwapFile swapFile = new JRSwapFile("src/main/resources/tmp", 4096, 200);
            JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(50, swapFile, true);
            parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
            parameters.put("orderPackageDatasetParam", dataSource);

            JasperPrint jasperPrint = null;
            try {
                jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
                JasperExportManager.exportReportToPdfFile(jasperPrint,"src/main/resources/tmp/fuck.pdf");
            } finally {
                // 7. Cleanup resources used by the virtualizer
                virtualizer.cleanup();
            }

        } catch (JRException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        return ResponseUtils.success("SIuu");
    }

    @GetMapping("/revenue/report/export/info")
    public ResponseEntity<?> exportRevenueReport (
            @RequestParam(required = false) FinancialReportFilterType filterType,
            @RequestParam(required = false) String filterValue,
            @RequestParam(required = false) Long revenueReportId,
            @RequestParam(required = false) String reportType
    ) {
        return ResponseUtils.success(
                service.getExportRevenueReportReal(
                        RevenueReportExportInfoRequest.builder()
                                .revenueReportId(revenueReportId)
                                .reportType(reportType)
                                .filterType(filterType)
                                .filterValue(filterValue)
                                .build()
                )
        );
    }

    private final FinancialService financialService;

    @GetMapping("/revenue/report/export/test")
    public void testRevenueExport(
            @RequestParam(required = false) FinancialReportFilterType filterType,
            @RequestParam(required = false) String filterValue,
            @RequestParam(required = false) Long revenueReportExportId
    ) {
        financialService.exportRevenueReport(RevenueReportExportMsg.builder()
                .revenueReportExportId(revenueReportExportId)
                .financialReportRequest(FinancialReportRequest.builder()
                        .filterType(filterType.toString())
                        .filterValue(filterValue)
                        .build())
                .build());
    }

}
