package com.vtp.vipo.seller.config.mq.kafka;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * This class represents a message data with a unique ID, subject, and content. It uses the Lombok
 * library to generate getters for the fields and a no-argument constructor for the class.
 * The @Getter annotation is used to generate getters for the fields. The @NoArgsConstructor
 * annotation is used to generate a no-argument constructor for the class. The class provides
 * several constructors to create a MessageData object with different parameters. The class also
 * provides a method to update the message ID. The class is used in the event handling system.
 *
 * @author haidv
 * @version 1.0
 */
@Getter
@NoArgsConstructor
public class MessageData<T> {

    /** The message ID. It is a unique identifier for the message. */
    private String messageId;

    /** The subject of the message. */
    private String subject;

    /**
     * The content of the message. It is a generic type, allowing for flexibility in the type of
     * content that can be stored.
     */
    private T content;

    /**
     * This constructor is used to create a MessageData object with the given content. The message ID
     * is generated using a random UUID.
     *
     * @param content the content of the message
     */
    public MessageData(T content) {
        this.messageId = UUID.randomUUID().toString();
        this.content = content;
    }

    /**
     * This constructor is used to create a MessageData object with the given subject and content. The
     * message ID is generated using a random UUID.
     *
     * @param subject the subject of the message
     * @param content the content of the message
     */
    public MessageData(String subject, T content) {
        this(content);
        this.subject = subject;
    }

    /**
     * This method is used to update the message ID.
     *
     * @param messageId the new message ID
     */
    public void updateMessageId(String messageId) {
        this.messageId = messageId;
    }
}
