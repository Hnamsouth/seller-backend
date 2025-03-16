package com.vtp.vipo.seller.config.db.redis;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author haidv
 * @version 1.0
 */
@Setter
@Getter
@NoArgsConstructor
public class HistoryMessage implements Serializable {
    private String messageId;
    private String destination;
    private String brokerType;
    private LocalDateTime consumeAt;

    public HistoryMessage(String messageId, String destination, String brokerType) {
        this.messageId = messageId;
        this.consumeAt = LocalDateTime.now();
        this.destination = destination;
        this.brokerType = brokerType;
    }
}
