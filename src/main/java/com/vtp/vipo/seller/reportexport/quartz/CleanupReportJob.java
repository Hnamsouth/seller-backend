package com.vtp.vipo.seller.reportexport.quartz;


import com.vtp.vipo.seller.common.exception.VipoFailedToExecuteException;
import com.vtp.vipo.seller.services.order.ReportExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class CleanupReportJob extends QuartzJobBean {

    private final ReportExportService reportExportService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        try {
            log.info("Starting cleanup of old report exports on node: {}",
                    context.getScheduler().getSchedulerInstanceId());
            reportExportService.deleteOldReportOccasionally();
            log.info("Cleanup of old report exports completed on node: {}",
                    context.getScheduler().getSchedulerInstanceId());
        } catch (SchedulerException e) {
            log.error("Failed to execute cleanup job", e);
            throw new VipoFailedToExecuteException(e.getLocalizedMessage());
        }
    }
}