package com.vtp.vipo.seller.common.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CacheUtils {

    private static RedisTemplate<String, Object> redisTemplate;

    public CacheUtils(RedisTemplate<String, Object> redisTemplate) {
        CacheUtils.redisTemplate = redisTemplate;
    }

    public static Boolean checkExistKey(String key) {
        return  redisTemplate.hasKey(key);
    }

    public static void addCache(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public static void addCache(String key, Object value, Long duration ) {
        redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(duration));
    }

    public static void addCacheToTopic(String key, Object value) {
        redisTemplate.convertAndSend("user-cache",value);
    }

    public static Object getCache(String key) {
        return redisTemplate.opsForValue().get(key);
    }

}
