package com.vtp.vipo.seller.config.mq.kafka;

import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class DLQMessageData<T> {


    /** The message ID. It is a unique identifier for the message. */
    @Builder.Default
    private String messageId = UUID.randomUUID().toString();

    /** The retry message ID. */
    @Builder.Default
    private String dlqMessageId = UUID.randomUUID().toString();

    /** The message data. It contains the data of the message. */
    private MessageData<T> data;

    /** The topic of the message. */
    private String topic;

    /** The source of the message. */
    private String source;

    /** The destination of the message. */
    private String destination;

    private String deadLetterQueue;

    private String failedMessage;

}
