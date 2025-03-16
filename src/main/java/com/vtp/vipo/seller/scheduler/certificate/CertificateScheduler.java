package com.vtp.vipo.seller.scheduler.certificate;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CertificateScheduler {
    final Scheduler scheduler;

    static String CRON_SCHEDULE_DELETE_CERTIFICATE = "0 0/10 * * * ?";

    @PostConstruct
    public void scheduleJobs() {
        try {
            log.info("Initializing DeleteCertificateJob...");
            scheduleDeleteCertificate();
            log.info("DeleteCertificateJob scheduled successfully.");
        } catch (Exception e) {
            log.error("Error initializing DeleteCertificateJob: ", e);
        }
    }

    private void scheduleDeleteCertificate() throws SchedulerException {
        JobKey jobKey = new JobKey("deleteCertificateJob", "DEFAULT");
        JobDetail jobDetail = JobBuilder.newJob(DeleteCertificateJob.class)
                .withIdentity(jobKey)
                .storeDurably()
                .build();

        // Add or replace the job
        scheduler.addJob(jobDetail, true);

        TriggerKey triggerKey = new TriggerKey("deleteCertificateTrigger", "DEFAULT");
        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(triggerKey)
                .withSchedule(CronScheduleBuilder.cronSchedule(CRON_SCHEDULE_DELETE_CERTIFICATE))
                .build();

        // Replace the existing trigger
        scheduler.rescheduleJob(triggerKey, trigger);
    }
}
