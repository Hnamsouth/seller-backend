package com.vtp.vipo.seller.scheduler.financial;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
// @Profile({"local", "dev"}) // Enable if running in local environment
@Profile("dev")
public class FinancialScheduler {

    @Value("${custom.properties.total-visits.cron}")
    private String cronScheduleTotalVisits;

    @Value("${custom.properties.revenue-report.cron}")
    private String cronScheduleRevenue;

    @Value("${custom.properties.job-key.prefix}")
    private String jobKeyPrefix;

    @Bean
    public JobDetail totalVisitsJobDetail() {
        return JobBuilder.newJob(TotalVisitsScheduler.class)
                .withIdentity(jobKeyPrefix + "totalVisitsSchedulerJob", "totalVisitsGroup")
                .storeDurably()
                .withDescription("Job để thực hiện tính toán lượt truy cập")
                .build();
    }

    @Bean
    public Trigger totalVisitsJobTrigger(JobDetail totalVisitsJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(totalVisitsJobDetail)
                .withIdentity(jobKeyPrefix + "totalVisitsSchedulerTrigger", "totalVisitsGroup")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronScheduleTotalVisits))
                .withDescription("Trigger cho job tính toán lượt truy cập")
                .build();
    }

    @Bean
    public JobDetail revenueDataJobDetail() {
        return JobBuilder.newJob(AggregateRevenueDataScheduler.class)
                .withIdentity(jobKeyPrefix + "aggregateRevenueDataSchedulerJob", "revenueDataGroup")
                .storeDurably()
                .withDescription("Job để tổng hợp dữ liệu doanh thu")
                .build();
    }

    @Bean
    public Trigger revenueDataJobTrigger(JobDetail revenueDataJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(revenueDataJobDetail)
                .withIdentity(jobKeyPrefix + "aggregateRevenueDataSchedulerTrigger", "revenueDataGroup")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronScheduleRevenue))
                .withDescription("Trigger cho job tổng hợp dữ liệu doanh thu")
                .build();
    }
}




