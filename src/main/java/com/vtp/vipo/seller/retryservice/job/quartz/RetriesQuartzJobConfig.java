package com.vtp.vipo.seller.retryservice.job.quartz;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RetriesQuartzJobConfig {

    @Bean
    public JobDetail handleRetriesMessageJobDetail() {
        return JobBuilder.newJob(HandleRetriesMessageJob.class)
                .withIdentity("handleRetriesMessageJob", "retriesGroup")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger handleRetriesMessageTrigger() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(1)
                .repeatForever()
                .withMisfireHandlingInstructionIgnoreMisfires();

        return TriggerBuilder.newTrigger()
                .forJob(handleRetriesMessageJobDetail())
                .withIdentity("handleRetriesMessageTrigger", "retriesGroup")
                .withSchedule(scheduleBuilder)
                .build();
    }
}

