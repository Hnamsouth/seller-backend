package com.vtp.vipo.seller.config.mq.kafka;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * This class represents a retry message data with a unique ID, message data, topic, source,
 * destination, number of retries, repeat count, delay time, pre-execution time, and status. It uses
 * the Lombok library to generate getters and setters for the fields and a no-argument constructor
 * for the class. The @Getter and @Setter annotations are used to generate getters and setters for
 * the fields. The class provides several constructors to create a RetriesMessageData object with
 * different parameters. The class also provides methods to increment the number of retries and to
 * delete retries. The class is used in the event handling system.
 *
 * @author haidv
 * @version 1.0
 */
@Getter
@Setter
public class RetriesMessageData<T> {

    /** The message ID. It is a unique identifier for the message. */
    private String messageId;

    /** The retry message ID. */
    private String retryMessageId;

    /** The message data. It contains the data of the message. */
    private MessageData<T> data;

    /** The topic of the message. */
    private String topic;

    /** The source of the message. */
    private String source;

    /** The destination of the message. */
    private String destination;

    /**
     * The number of retries. It represents the number of times the message handling should be retried
     * in case of failure.
     */
    private Integer retriesNo;

    /** The repeat count. */
    private Integer repeatCount;

    /** The delay time. */
    private Long delayTime;

    /**
     * The pre-execution time. It represents the time before the execution of the message handling.
     */
    private LocalDateTime preExecuteAt;

    /** The status of the retry message data. It can be INSERT, DELETE, or UPDATE. */
    private RetriesMessageDataStatus status;

    private String deadLetterQueue;

    private String failedMessage;

    /**
     * This constructor is used to create a RetriesMessageData object. The message ID is generated
     * using a random UUID. The number of retries is set to 1. The status is set to INSERT.
     */
    public RetriesMessageData() {
        this.messageId = UUID.randomUUID().toString();
        this.retriesNo = 1;
        this.status = RetriesMessageDataStatus.INSERT;
    }

    /**
     * This constructor is used to create a RetriesMessageData object with the given retry message ID,
     * message data, topic, delay time, and repeat count. The message ID is generated using a random
     * UUID. The number of retries is set to 1. The status is set to INSERT. The pre-execution time is
     * set to the current time.
     *
     * @param retryMessageId the retry message ID
     * @param data the message data
     * @param topic the topic
     * @param delayTime the delay time
     * @param repeatCount the repeat count
     */
    public RetriesMessageData(
            String retryMessageId,
            MessageData<T> data,
            String topic,
            long delayTime,
            Integer repeatCount,
            String deadLetterQueue
            ) {
        this();
        this.retryMessageId = retryMessageId;
        this.data = data;
        this.topic = topic;
        this.delayTime = delayTime;
        this.repeatCount = repeatCount;
        this.preExecuteAt = LocalDateTime.now();
        this.deadLetterQueue = deadLetterQueue;
    }

    /**
     * This method is used to increment the number of retries. The message ID is generated using a
     * random UUID. The message data, source, destination, topic, and repeat count are set to null.
     * The pre-execution time is set to the current time. The status is set to UPDATE.
     *
     * @return the updated RetriesMessageData object
     */
    public RetriesMessageData<T> incrementRetriesNo() {
        this.messageId = UUID.randomUUID().toString();
        this.retriesNo = this.retriesNo + 1;
        this.data = null;
        this.source = null;
        this.destination = null;
        this.topic = null;
        this.repeatCount = null;
        this.preExecuteAt = LocalDateTime.now();
        this.status = RetriesMessageDataStatus.UPDATE;
        return this;
    }

    /**
     * This method is used to delete retries. The message ID is generated using a random UUID. The
     * number of retries, message data, source, destination, topic, delay time, and repeat count are
     * set to null. The status is set to DELETE.
     *
     * @return the updated RetriesMessageData object
     */
    public RetriesMessageData<T> deleteRetries() {
        this.messageId = UUID.randomUUID().toString();
        this.retriesNo = null;
        this.data = null;
        this.source = null;
        this.destination = null;
        this.topic = null;
        this.delayTime = null;
        this.repeatCount = null;
        this.status = RetriesMessageDataStatus.DELETE;
        return this;
    }

    /**
     * This enum represents the status of the retry message data. It can be INSERT, DELETE, or UPDATE.
     */
    public enum RetriesMessageDataStatus {
        INSERT,
        DELETE,
        UPDATE
    }
}
