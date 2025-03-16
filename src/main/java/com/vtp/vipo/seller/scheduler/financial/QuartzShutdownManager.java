package com.vtp.vipo.seller.scheduler.financial;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzShutdownManager {

    private final Scheduler scheduler;

    @PreDestroy
    public void shutdownScheduler() {
        try {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown(true);
                log.info("Scheduler shutdown successfully.");
            }
        } catch (SchedulerException e) {
            log.error("Error shutting down scheduler: ", e);
        }
    }
}

