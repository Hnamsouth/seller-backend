package com.vtp.vipo.seller.reportexport.quartz;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Slf4j
public class QuartzConfig {

    @Value("${custom.properties.report-export.cleanup.cron}")
    private String cleanUpReportCron;

    @Value("${custom.properties.report-export.cleanup.cron-dev:0 0 * * * ?}")
    private String cleanUpReportCronDev;

    @Bean
    public JobDetail cleanupReportJobDetail() {
        return JobBuilder.newJob(CleanupReportJob.class)
                .withIdentity("cleanupReportJob", "reportGroup") // Add group
                .storeDurably()
                .requestRecovery(true) // Add recovery
                .build();
    }

    @Bean
    public Trigger cleanupReportJobTrigger() { // Remove JobDetail parameter
        return TriggerBuilder.newTrigger()
                .forJob(cleanupReportJobDetail()) // Call the method directly
                .withIdentity("cleanupReportTrigger", "reportGroup") // Add group
                .withSchedule(
                        CronScheduleBuilder
                                .cronSchedule(cleanUpReportCron)
                                .withMisfireHandlingInstructionDoNothing()
                )
                .build();
    }

    @Profile("dev")
    @Bean
    public JobDetail cleanupReportJobDetailDev() {
        return JobBuilder.newJob(CleanupReportJob.class)
                .withIdentity("cleanupReportJobDev", "reportGroup") // Add group
                .storeDurably()
                .requestRecovery(true) // Add recovery
                .build();
    }

    @Profile("dev")
    @Bean
    public Trigger cleanupReportJobTriggerDev() { // Remove JobDetail parameter
        return TriggerBuilder.newTrigger()
                .forJob(cleanupReportJobDetail()) // Call the method directly
                .withIdentity("cleanupReportTriggerDev", "reportGroup") // Add group
                .withSchedule(
                        CronScheduleBuilder
                                .cronSchedule(cleanUpReportCronDev)
                                .withMisfireHandlingInstructionDoNothing()
                )
                .build();
    }

    @PostConstruct
    public void logCronSchedule() {
        log.info("Cleanup report job scheduled with cron: {}", cleanUpReportCron);
    }
}