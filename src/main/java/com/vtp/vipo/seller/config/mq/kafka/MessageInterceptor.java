package com.vtp.vipo.seller.config.mq.kafka;

import com.vtp.vipo.seller.VipoSellerApplication;
import com.vtp.vipo.seller.common.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for intercepting and handling messages in the system. It provides
 * methods to send messages to a Kafka queue and to convert and send retry events. The class uses
 * the Lombok library to generate a required argument constructor for the class. The @Component
 * annotation is used to indicate that the class is a Spring component. The @RequiredArgsConstructor
 * annotation is used to generate a required argument constructor for the class. The class also
 * defines several constants for logging. The class is used in the event handling system.
 *
 * @author haidv
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageInterceptor {

    /** The log message for the start of pushing a message to a queue. */
    private static final String LOG_START =
            "Start push message to queue: {} messageId: {} with payload: {}";

    /** The log message for the end of pushing a message to a queue. */
    private static final String LOG_END = "End push message to queue: {} messageId: {}";

    /** The Kafka template for sending messages to a Kafka queue. */
    @SuppressWarnings("rawtypes")
    private final KafkaTemplate kafkaTemplate;

    /** The name of the Kafka topic for retry events. */
    @Value("${custom.properties.kafka.topic.retries-event.name}")
    private String retriesEventTopic;

    /**
     * This method is used to convert a retry message data to JSON and send it to the Kafka queue for
     * retry events.
     *
     * @param payload the retry message data
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void convertAndSendRetriesEvent(RetriesMessageData payload) {
        log.info(LOG_START, retriesEventTopic, payload.getMessageId(), JsonUtils.toJson(payload));
        if (payload.getStatus().equals(RetriesMessageData.RetriesMessageDataStatus.INSERT)) {
            payload.setSource(VipoSellerApplication.APPLICATION_NAME.toUpperCase());
            payload.setDestination(
                    String.format("%s.%s.%s", payload.getTopic(), payload.getSource(), "RETRIES"));
        }
        kafkaTemplate.send(retriesEventTopic, payload.getMessageId(), JsonUtils.toJson(payload));
        log.info(LOG_END, retriesEventTopic, payload.getMessageId());
    }

    /**
     * This method is used to convert a message data to JSON and send it to a specified Kafka queue.
     *
     * @param queueName the name of the Kafka queue
     * @param payload the message data
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void convertAndSend(String queueName, MessageData payload) {
        var payloadJson = JsonUtils.toJson(payload);
        log.info(LOG_START, queueName, payload.getMessageId(), payloadJson);
        kafkaTemplate.send(queueName, payloadJson);
        log.info(LOG_END, queueName, payload.getMessageId());
    }

    /**
     * This method is used to convert a message data to JSON and send it to a specified Kafka queue
     * with a specified key.
     *
     * @param queueName the name of the Kafka queue
     * @param key the key
     * @param payload the message data
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void convertAndSend(String queueName, String key, MessageData payload) {
        var payloadJson = JsonUtils.toJson(payload);
        log.info(LOG_START, queueName, payload.getMessageId(), payloadJson);
        kafkaTemplate.send(queueName, key, payloadJson);
        log.info(LOG_END, queueName, payload.getMessageId());
    }

    /**
     * This method is used to convert a retry message data to JSON and send it to a specified Kafka
     * queue.
     *
     * @param payload the retry message data
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void convertAndSend(RetriesMessageData payload) {
        var payloadJson = JsonUtils.toJson(payload);
        log.info(LOG_START, payload.getDestination(), payload.getMessageId(), payloadJson);
        kafkaTemplate.send(payload.getDestination(), payload.getRetryMessageId(), payloadJson);
        log.info(LOG_END, payload.getDestination(), payload.getMessageId());
    }

    public <T> void convertAndSendDLQEvent(String dlqTopicName, DLQMessageData<T> input) {
        String data = JsonUtils.toJson(input);
        log.info(LOG_START, retriesEventTopic, input.getMessageId(), data);
        kafkaTemplate.send(dlqTopicName, input.getMessageId(), data);
        log.info(LOG_END, retriesEventTopic, input.getMessageId());
    }
}