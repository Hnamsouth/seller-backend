package com.vtp.vipo.seller.config.mq.kafka;

import com.vtp.vipo.seller.common.constants.RequestConstant;
import com.vtp.vipo.seller.common.utils.JsonUtils;
import com.vtp.vipo.seller.config.db.redis.HistoryMessage;
import com.vtp.vipo.seller.config.db.redis.HistoryMessageRepository;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.Acknowledgment;

/**
 * This abstract class represents a message listener that handles messages in the system. It
 * provides methods to listen to messages, handle message events, and check if the generic type is
 * supported. The class uses the Lombok library to generate a required argument constructor for the
 * class. The @Autowired annotation is used to autowire the MessageInterceptor and
 * HistoryMessageRepository beans. The class is extended by classes that implement specific message
 * listeners. The class is used in the event handling system.
 *
 * @author haidv
 * @version 1.0
 */
@Slf4j
public abstract class MessageListener<T> {

    /** The repository for history messages. */
    protected HistoryMessageRepository historyMessageRepository;

    /** The interceptor for messages. */
    protected MessageInterceptor messageInterceptor;

    /**
     * This method is used to autowire the MessageInterceptor bean.
     *
     * @param messageInterceptor the MessageInterceptor bean
     */
    @Autowired
    public final void setMessageInterceptor(MessageInterceptor messageInterceptor) {
        this.messageInterceptor = messageInterceptor;
    }

    /**
     * This method is used to autowire the HistoryMessageRepository bean.
     *
     * @param historyMessageRepository the HistoryMessageRepository bean
     */
    @Autowired
    public final void setHistoryMessageRepository(HistoryMessageRepository historyMessageRepository) {
        this.historyMessageRepository = historyMessageRepository;
    }

    /**
     * This method is used to check if the generic type is supported. It is called after the bean
     * properties have been set.
     */
    @PostConstruct
    public void checkGenericTypeSupport() {
        try {
            this.getMessageContentType();
        } catch (Exception e) {
            log.error("MessageListener generic type support failed.");
            throw e;
        }
    }

    /**
     * This method is used to listen to a message. It initializes the listener, gets the message data,
     * checks if the message has been processed, handles the message event, and acknowledges the
     * message.
     *
     * @param data the message data
     * @param topic the topic
     * @param partition the partition
     * @param offset the offset
     * @param acknowledgment the acknowledgment
     */
    @SuppressWarnings("unused")
    public void messageListener(
            String data, String topic, String partition, String offset, Acknowledgment acknowledgment) {
        messageListener(data, topic, partition, offset, acknowledgment, 0, 0, null);
    }

    /**
     * This method is used to listen to a message with a delay time and a repeat count. It performs
     * several operations: 1. Initializes the listener. 2. Converts the message data from JSON to a
     * MessageData object. 3. If the MessageData object is null, it logs an info message, acknowledges
     * the message, clears the thread context, and returns. 4. If the MessageData object's message ID
     * is blank, it updates the message ID. 5. Tries to put the message into the history message
     * repository. 6. If the message has already been processed, it logs a warning and returns. 7.
     * Handles the message event. 8. If the handling is successful, it logs an info message. 9. If an
     * exception is thrown during the handling of the message event and the repeat count is greater
     * than 0, it sends a retry event. 10. Finally, it acknowledges the message and clears the thread
     * context.
     *
     * @param data the message data
     * @param topic the topic
     * @param partition the partition
     * @param offset the offset
     * @param acknowledgment the acknowledgment
     * @param delayTime the delay time
     * @param repeatCount the repeat count
     */
    public void messageListener(
            String data,
            String topic,
            String partition,
            String offset,
            Acknowledgment acknowledgment,
            long delayTime,
            Integer repeatCount,
            String dlqTopicName
    ) {
        this.initListener(topic, partition, offset, data);

        MessageData<T> input =
                JsonUtils.fromJson(data, MessageData.class, this.getMessageContentType());

//        T dataObj = JsonMapperUtils.fromJson(data, this.getMessageContentType());
//        MessageData<T> input = new MessageData<>(dataObj);

        if (input == null) {
            log.info("[KafkaConsumer][{}][{}][{}]  ignore!", topic, partition, offset);
            acknowledgment.acknowledge();
            ThreadContext.clearAll();
            return;
        }
        if (StringUtils.isBlank(input.getMessageId())) {
            input.updateMessageId(String.format("%s_%s_%s", topic, partition, offset));
        }
        try {
            if (Boolean.FALSE.equals(
                    historyMessageRepository.put(
                            new HistoryMessage(input.getMessageId(), topic, RequestConstant.BROKER_KAFKA)))) {
                log.warn(
                        "[KafkaConsumer][{}][{}][{}]  message has been processed", topic, partition, offset);
                return;
            }
            this.handleMessageEvent(topic, partition, offset, input);
            log.info("[KafkaConsumer][{}][{}][{}]  successful!", topic, partition, offset);
        } catch (Exception e) {
            log.error("[KafkaConsumer][{}][{}][{}]  Exception revert ", topic, partition, offset, e);
            if (repeatCount > 0) {
                messageInterceptor.convertAndSendRetriesEvent(
                        new RetriesMessageData<T>(input.getMessageId(), input, topic, delayTime, repeatCount, dlqTopicName)
                );
            } else if (StringUtils.isNotBlank(dlqTopicName)) { //send to dlq
                var dlqMsgData = DLQMessageData. <T>builder()
                        .data(input)
                        .failedMessage(e.getLocalizedMessage())
                        .build();
                messageInterceptor.convertAndSendDLQEvent(dlqTopicName, dlqMsgData);
            }

        } finally {
            acknowledgment.acknowledge();
            ThreadContext.clearAll();
        }
    }

    /**
     * This method is used to listen to a retry message. It initializes the listener, gets the retry
     * message data, checks if the message has been processed, handles the message event, and
     * acknowledges the message. If an exception is thrown during the handling of the message event,
     * it sends a retry event.
     *
     * @param data the message data
     * @param topic the topic
     * @param partition the partition
     * @param offset the offset
     * @param acknowledgment the acknowledgment
     */
    public void messageRetriesListener(
            String data, String topic, String partition, String offset, Acknowledgment acknowledgment) {
        this.initListener(topic, partition, offset, data);
        RetriesMessageData<T> input =
                JsonUtils.fromJson(data, RetriesMessageData.class, this.getMessageContentType());
        if (input == null) {
            log.info("[KafkaConsumer][{}][{}][{}]  ignore!", topic, partition, offset);
            acknowledgment.acknowledge();
            ThreadContext.clearAll();
            return;
        }
        MessageData<T> retryData = input.getData();
        if (retryData == null) {
            log.info("[KafkaConsumer][{}][{}][{}]  retryData ignore!", topic, partition, offset);
            acknowledgment.acknowledge();
            ThreadContext.clearAll();
            return;
        }
        try {
            if (Boolean.FALSE.equals(
                    this.historyMessageRepository.put(
                            new HistoryMessage(input.getMessageId(), topic, RequestConstant.BROKER_KAFKA)))) {
                log.warn(
                        "[KafkaConsumer][{}][{}][{}]  message has been processed", topic, partition, offset);
                return;
            }
            handleMessageEvent(topic, partition, offset, retryData);
            messageInterceptor.convertAndSendRetriesEvent(input.deleteRetries());
            log.info("[KafkaConsumer][{}][{}][{}]  successful!", topic, partition, offset);
        } catch (Exception e) {
            log.error("[KafkaConsumer][{}][{}][{}]  Exception revert ", topic, partition, offset, e);
            input.setFailedMessage(e.getLocalizedMessage());
            messageInterceptor.convertAndSendRetriesEvent(input.incrementRetriesNo());
        } finally {
            acknowledgment.acknowledge();
            ThreadContext.clearAll();
        }
    }

    public void messageDLQListener(
            String data, String topic, String partition, String offset, Acknowledgment acknowledgment) {
        this.initListener(topic, partition, offset, data);
        DLQMessageData<T> input =
                JsonUtils.fromJson(data, DLQMessageData.class, this.getMessageContentType());
        if (input == null) {
            log.info("[KafkaConsumer][{}][{}][{}]  ignore!", topic, partition, offset);
            acknowledgment.acknowledge();
            ThreadContext.clearAll();
            return;
        }
        MessageData<T> dlqData = input.getData();
        if (dlqData == null) {
            log.info("[KafkaConsumer][{}][{}][{}]  retryData ignore!", topic, partition, offset);
            acknowledgment.acknowledge();
            ThreadContext.clearAll();
            return;
        }
        try {
            if (Boolean.FALSE.equals(
                    this.historyMessageRepository.put(
                            new HistoryMessage(input.getMessageId(), topic, RequestConstant.BROKER_KAFKA)))) {
                log.warn(
                        "[KafkaConsumer][{}][{}][{}]  message has been processed", topic, partition, offset);
                return;
            }
            handleDeadMessageEvent(topic, partition, offset, input);
            log.info("[KafkaConsumer][{}][{}][{}]  successful!", topic, partition, offset);
        } catch (Exception e) {
            log.error("[KafkaConsumer][{}][{}][{}]  Exception revert ", topic, partition, offset, e);
            //todo: determine if failing to process DLQ
            //todo: may be apply retrying
        } finally {
            acknowledgment.acknowledge();
            ThreadContext.clearAll();
        }
    }

    /**
     * This method is used to initialize the listener. It puts the request ID, broker type, and
     * message event in the thread context and logs the incoming message.
     *
     * @param topic the topic
     * @param partition the partition
     * @param offset the offset
     * @param data the message data
     */
    private void initListener(String topic, String partition, String offset, String data) {
        ThreadContext.put(RequestConstant.REQUEST_ID, UUID.randomUUID().toString());
        ThreadContext.put(RequestConstant.BROKER_TYPE, RequestConstant.BROKER_KAFKA);
        ThreadContext.put(RequestConstant.MESSAGE_EVENT, topic);
        log.info("[KafkaConsumer][{}][{}][{}] Incoming: {}", topic, partition, offset, data);
    }

    /**
     * This abstract method should be implemented by subclasses to handle a message event.
     *
     * @param topic the topic
     * @param partition the partition
     * @param offset the offset
     * @param input the message data
     */
    protected abstract void handleMessageEvent(
            String topic, String partition, String offset, MessageData<T> input);


    protected abstract void handleDeadMessageEvent(
            String topic, String partition, String offset, DLQMessageData<T> input);

    /**
     * This method is used to get the content type of the message. It gets the actual type argument of
     * the generic superclass of the class.
     *
     * @return the content type of the message
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Class getMessageContentType() {
        return (Class<T>)
                ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }
}

