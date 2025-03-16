//package com.vtp.vipo.seller.business.scheduler;
//
//import com.vtp.vipo.seller.services.order.ReportExportService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class ReportExportCleanupScheduler {
//
//    private final ReportExportService reportExportService;
//
//    /**
//     * Scheduled task to clean up old report exports.
//     * Runs daily at 23:59 (11:59 PM).
//     */
//    @Scheduled(cron = "${custom.properties.report-export.cleanup.cron}")
//    public void cleanupOldReportExports() {
//        log.info("Starting cleanup of old report exports.");
//        reportExportService.deleteOldReportOccasionally();
//        log.info("Cleanup of old report exports completed.");
//    }
//}
//
