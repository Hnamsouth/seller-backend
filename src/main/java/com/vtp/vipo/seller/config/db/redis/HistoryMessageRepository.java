package com.vtp.vipo.seller.config.db.redis;

import java.time.Duration;

import com.vtp.vipo.seller.VipoSellerApplication;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author haidv
 * @version 1.0
 */
@Repository
@RequiredArgsConstructor
public class HistoryMessageRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public Boolean put(HistoryMessage historyMessage) {
        return redisTemplate
                .opsForValue()
                .setIfAbsent(composeHeader(historyMessage), historyMessage, Duration.ofHours(12));
    }

    public Boolean get(HistoryMessage historyMessage) {
        return redisTemplate.hasKey(composeHeader(historyMessage));
    }

    private String composeHeader(HistoryMessage historyMessage) {
        return String.format(
                "LOYALTY_HISTORY_MESSAGE:%s:%s:%s:%s",
                historyMessage.getBrokerType().toUpperCase(),
                VipoSellerApplication.APPLICATION_NAME.toUpperCase(),
                historyMessage.getDestination().toUpperCase(),
                historyMessage.getMessageId());
    }
}