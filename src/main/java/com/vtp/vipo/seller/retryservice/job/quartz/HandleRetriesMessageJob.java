package com.vtp.vipo.seller.retryservice.job.quartz;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.vtp.vipo.seller.common.constants.RequestConstant;
import com.vtp.vipo.seller.common.utils.DateUtils;
import com.vtp.vipo.seller.config.db.redis.HistoryMessage;
import com.vtp.vipo.seller.config.db.redis.HistoryMessageRepository;
import com.vtp.vipo.seller.config.mq.kafka.MessageInterceptor;
import com.vtp.vipo.seller.config.mq.kafka.RetriesMessageData;
import com.vtp.vipo.seller.retryservice.dao.entity.RetriesMessage;
import com.vtp.vipo.seller.retryservice.dao.repository.RetriesMessageRepository;
import com.vtp.vipo.seller.retryservice.mapper.RetryMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.ThreadContext;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class HandleRetriesMessageJob extends QuartzJobBean {

    protected static final Logger LOGGER = LoggerFactory.getLogger(HandleRetriesMessageJob.class);
//
//    private final ScheduleLogRepository scheduleLogRepository;
//
//    private final ScheduleRepository scheduleRepository;

    private final RetriesMessageRepository retriesMessageRepository;

    private final MessageInterceptor messageInterceptor;

    private final RetryMessageMapper retryMessageMapper;

    private final HistoryMessageRepository historyMessageRepository;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        log.info("check");
        ThreadContext.put(RequestConstant.REQUEST_ID, UUID.randomUUID().toString());
        JobKey jobKey = context.getJobDetail().getKey();
//        ScheduleInfo scheduleInfo =
//                scheduleRepository
//                        .findByJobNameAndJobGroupAndDeletedFalse(jobKey.getName(), jobKey.getGroup())
//                        .orElseThrow(() -> new BaseException(CommonErrorCode.JOB_NOT_EXISTED));
//        ScheduleLog scheduleLog =
//                ScheduleLog.builder()
//                        .jobId(scheduleInfo.getId())
//                        .status(StatusJob.STARTED)
//                        .startTime(ZonedDateTime.now())
//                        .build();
//        scheduleLog = scheduleLogRepository.save(scheduleLog);

        try {
            List<RetriesMessage> retriesMessages = retriesMessageRepository
                    .findByRetriesActivated(DateUtils.getCurrentLocalDateTime());
            if (ObjectUtils.isEmpty(retriesMessages))
                return;
            retriesMessages
                    .forEach(
                            retriesMessage -> {
                                RetriesMessageData retriesMessageData = retryMessageMapper.toRetriesMessageData(retriesMessage);
                                if (ObjectUtils.isEmpty(retriesMessageData))
                                    return;
                                retriesMessageData.setMessageId(getMessageIdForRetriesMessage(retriesMessage));
                                var historyMessage = new HistoryMessage(
                                        retriesMessageData.getMessageId(),
                                        retriesMessageData.getDestination(),
                                        RequestConstant.BROKER_KAFKA
                                );
                                Boolean isSent = historyMessageRepository.get(historyMessage);
                                if (ObjectUtils.isEmpty(isSent) || Boolean.FALSE.equals(isSent)) {
                                    messageInterceptor.convertAndSend(retriesMessageData);
                                }
                            }
                    );
        } catch (Exception e) {
//            scheduleLog.setStatus(StatusJob.FAILED);
//            scheduleLog.setExceptionInfo(e.getMessage());
            LOGGER.error(e.getMessage(), e);
        } finally {
//            scheduleLog.setEndTime(ZonedDateTime.now());
//            scheduleLogRepository.save(scheduleLog);
//            scheduleInfo.setJobStatus(StatusJob.STARTED);
//            scheduleInfo.setExecuteLastTime(ZonedDateTime.now());
//            scheduleRepository.save(scheduleInfo);
        }
    }

    private String getMessageIdForRetriesMessage(RetriesMessage retriesMessage) {
        return "retry-"+ retriesMessage.getId()+"-retryNo-"+retriesMessage.getRetriesNo();
    }

}
