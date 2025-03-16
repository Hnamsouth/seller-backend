package com.vtp.vipo.seller.services.redis;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisService {

    RedisTemplate<String, String> redisStringTemplate;

    public String getCache(String key) {
        return redisStringTemplate.opsForValue().get(key);
    }

    public Set<String> scan(String pattern) {
        Set<String> matchedKeys = new HashSet<>();

        // Chỉ định pattern, không dùng count(...) để Redis dùng giá trị mặc định
        ScanOptions scanOptions = ScanOptions.scanOptions().match(pattern).build();

        // Sử dụng try-with-resources, tự động đóng cursor
        try (Cursor<byte[]> cursor = (Cursor<byte[]>) redisStringTemplate.execute((RedisConnection connection) ->
                connection.scan(scanOptions))) {

            if (cursor != null) {
                while (cursor.hasNext()) {
                    byte[] keyBytes = cursor.next();
                    String key = new String(keyBytes, StandardCharsets.UTF_8);
                    matchedKeys.add(key);
                }
            }
        } catch (Exception e) {
            log.error("Error closing Redis cursor: {}", e.getMessage(), e);
        }

        return matchedKeys;
    }

    public Long getValueAsLong(String key) {
        String value = redisStringTemplate.opsForValue().get(key);
        if (ObjectUtils.isEmpty(value)) {
            return 0L;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    public void deleteKeys(Collection<String> keys) {
        redisStringTemplate.delete(keys);
    }

    @Bean
    public RedisTemplate<String, String> redisStringTemplate(final RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}
