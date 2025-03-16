package com.vtp.vipo.seller.retryservice.event.kafka;


import com.vtp.vipo.seller.common.constants.RequestConstant;
import com.vtp.vipo.seller.common.utils.JsonMapperUtils;
import com.vtp.vipo.seller.common.utils.JsonUtils;
import com.vtp.vipo.seller.config.db.redis.HistoryMessage;
import com.vtp.vipo.seller.config.db.redis.HistoryMessageRepository;
import com.vtp.vipo.seller.config.mq.kafka.DLQMessageData;
import com.vtp.vipo.seller.config.mq.kafka.MessageData;
import com.vtp.vipo.seller.config.mq.kafka.RetriesMessageData;
import com.vtp.vipo.seller.retryservice.dao.repository.RetriesMessageRepository;
import com.vtp.vipo.seller.retryservice.mapper.RetryMessageMapper;
import io.swagger.v3.core.util.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author haidv
 * @version 1.0
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class MessageListener {

    //todo: do telegram bot
//    private final TelegramService telegramService;

    private final RetriesMessageRepository retriesMessageRepository;

    private final HistoryMessageRepository historyMessageRepository;

    private final RetryMessageMapper retryMessageMapper;

    private final KafkaTemplate kafkaTemplate;

    @KafkaListener(
            topics = "${custom.properties.kafka.topic.retries-event.name}",
            groupId = "${custom.properties.messaging.kafka.groupId}",
            concurrency = "1",
            containerFactory = "kafkaListenerContainerFactory")
    public void topupGameEventListener(
            String data,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) String partition,
            @Header(KafkaHeaders.OFFSET) String offset,
            Acknowledgment acknowledgment) {
        ThreadContext.put(RequestConstant.REQUEST_ID, UUID.randomUUID().toString());
        ThreadContext.put(RequestConstant.KAFKA_EVENT, topic);
        log.info("[KafkaConsumer][{}][{}][{}] Incoming: {}", topic, partition, offset, data);
        RetriesMessageData input = JsonUtils.fromJson(data, RetriesMessageData.class);
        if (input == null) {
            log.info("[KafkaConsumer][{}][{}][{}]  ignore!", topic, partition, offset);
            acknowledgment.acknowledge();
            ThreadContext.clearAll();
            return;
        }
        try {
            if (
                    Boolean.FALSE
                            .equals(
                                    historyMessageRepository.put(
                                            new HistoryMessage(
                                                    input.getMessageId(), topic, RequestConstant.BROKER_KAFKA
                                            )
                                    )
                            )
            ) {
                log.warn(
                        "[KafkaConsumer][{}][{}][{}]  message has been processed", topic, partition, offset);
                return;
            }
            retriesMessageRepository
                    .findByMessageId(input.getRetryMessageId())
                    .ifPresentOrElse(
                            v -> {
                                if (input.getStatus().equals(RetriesMessageData.RetriesMessageDataStatus.DELETE)) {
                                    retriesMessageRepository.deleteByMessageId(input.getMessageId());
                                } else {
                                    v.setNextExecuteAt(input.getPreExecuteAt().plusSeconds(v.getDelayTime()));
                                    v.setRetriesNo(input.getRetriesNo());

                                    /* dead letter queue */
                                    if (
                                            v.getRetriesNo() > v.getRepeatCount()
                                            && StringUtils.isNotBlank(v.getDeadLetterQueue())
                                    ) {
                                        MessageData messageData = JsonMapperUtils.convertJsonToObject(v.getData(), MessageData.class);
                                        var dqlData = DLQMessageData.builder()
                                                .data(messageData)
                                                .failedMessage(input.getFailedMessage())
                                                .build();
                                        kafkaTemplate.send(v.getDeadLetterQueue(), input.getMessageId(), JsonMapperUtils.writeValueAsString(dqlData));
                                    }

                                    retriesMessageRepository.save(v);
                                }
                            },
                            () -> {
                                var msg = retryMessageMapper.toRetriesMessage(input);
                                msg.setNextExecuteAt(input.getPreExecuteAt().plusSeconds(input.getDelayTime()));
                                retriesMessageRepository.save(msg);
                            });
            log.info("[KafkaConsumer][{}][{}][{}]  successful!", topic, partition, offset);
        } catch (Exception e) {
            log.error("[KafkaConsumer][{}][{}][{}]  Exception revert ", topic, partition, offset, e);
//            telegramService.sendMessageErrorToAdmin(
//                    String.format("[KafkaConsumer][%s] handle message failure: %s", topic, data));
        } finally {
            acknowledgment.acknowledge();
            ThreadContext.clearAll();
        }
    }
}