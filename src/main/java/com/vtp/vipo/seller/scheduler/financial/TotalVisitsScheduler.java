package com.vtp.vipo.seller.scheduler.financial;

import com.vtp.vipo.seller.common.dao.entity.ProductDailyAnalyticEntity;
import com.vtp.vipo.seller.common.dao.repository.ProductDailyAnalyticEntityRepository;
import com.vtp.vipo.seller.services.redis.RedisService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@DisallowConcurrentExecution
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TotalVisitsScheduler extends QuartzJobBean {

    RedisService redisService;

    ProductDailyAnalyticEntityRepository productDailyAnalyticEntityRepository;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        log.info("[TotalVisitsScheduler] Start TotalVisitsScheduler job...");

        // Retrieve all keys from Redis matching the pattern "product:view:*"
        Set<String> redisKeys = redisService.scan("product:view:*");
        if (redisKeys.isEmpty()) {
            log.info("[TotalVisitsScheduler] No redis keys found for pattern product:view:*");
            return;
        }

        // Aggregate data from Redis into a Map<KeyInfo, Long>
        Map<KeyInfo, Long> aggregatedData = new HashMap<>();

        for (String key : redisKeys) {
            // New key format: product:view:{productId}:{merchantId}:{yyyyMMdd}
            // => parts[0] = "product"
            // => parts[1] = "view"
            // => parts[2] = productId
            // => parts[3] = merchantId
            // => parts[4] = yyyyMMdd
            String[] parts = key.split(":");
            if (parts.length != 5) {
                log.warn("[TotalVisitsScheduler] Key [{}] does not match the expected format: product:view:{productId}:{merchantId}:{yyyyMMdd}", key);
                continue;
            }

            // Parse the productId from the key
            Long productId;
            try {
                productId = Long.valueOf(parts[2]);
            } catch (NumberFormatException e) {
                log.warn("[TotalVisitsScheduler] Cannot parse productId from key {}", key);
                continue;
            }

            // Parse the merchantId from the key
            Long merchantId;
            try {
                merchantId = Long.valueOf(parts[3]);
            } catch (NumberFormatException e) {
                log.warn("[TotalVisitsScheduler] Cannot parse merchantId from key {}", key);
                continue;
            }

            // Parse the date from the key using the pattern "yyyyMMdd"
            String dateString = parts[4];
            LocalDate date;
            try {
                date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"));
            } catch (DateTimeParseException e) {
                log.warn("[TotalVisitsScheduler] Cannot parse date from key {}", key);
                continue;
            }

            // Retrieve the view count from Redis for the given key
            Long totalVisits = redisService.getValueAsLong(key);
            if (ObjectUtils.isEmpty(totalVisits)) {
                totalVisits = 0L;
            }

            // Group the data by (productId, merchantId, periodStart = date, periodEnd = date)
            KeyInfo keyInfo = new KeyInfo(productId, merchantId, date, date);
            // Sum the view counts if the key already exists
            aggregatedData.merge(keyInfo, totalVisits, Long::sum);
        }

        if (aggregatedData.isEmpty()) {
            log.info("[TotalVisitsScheduler] No valid data to process after parsing Redis keys");
            return;
        }

        // Retrieve existing entities from the database. Fetch all entities by productId, merchantId, periodStart, and periodEnd
        Set<Long> productIds = aggregatedData.keySet()
                .stream()
                .map(KeyInfo::getProductId)
                .collect(Collectors.toSet());

        Set<Long> merchantIds = aggregatedData.keySet()
                .stream()
                .map(KeyInfo::getMerchantId)
                .collect(Collectors.toSet());

        // Since periodStart and periodEnd are the same, extract dates from periodStart
        Set<LocalDate> dates = aggregatedData.keySet()
                .stream()
                .map(KeyInfo::getPeriodStart)
                .collect(Collectors.toSet());

        // Query the database in one call
        List<ProductDailyAnalyticEntity> existingEntities =
                productDailyAnalyticEntityRepository
                        .findAllByProductIdInAndMerchantIdInAndPeriodStartInAndPeriodEndIn(
                                productIds, merchantIds, dates, dates);

        // Create a map for easy lookup using KeyInfo as the key
        Map<KeyInfo, ProductDailyAnalyticEntity> existingMap = existingEntities.stream()
                .collect(Collectors.toMap(
                        e -> new KeyInfo(e.getProductId(), e.getMerchantId(), e.getPeriodStart(), e.getPeriodEnd()),
                        Function.identity()
                ));

        // Prepare the list of entities to be saved/updated
        List<ProductDailyAnalyticEntity> toSave = new ArrayList<>();
        for (Map.Entry<KeyInfo, Long> entry : aggregatedData.entrySet()) {
            KeyInfo keyInfo = entry.getKey();
            Long viewCount = entry.getValue();

            ProductDailyAnalyticEntity entity = existingMap.get(keyInfo);
            if (ObjectUtils.isNotEmpty(entity)) {
                // If the entity exists, update its viewCount
                entity.setViewCount(viewCount);
                entity.setUpdatedAt(LocalDateTime.now());
                toSave.add(entity);
            } else {
                // If it doesn't exist, create a new entity
                ProductDailyAnalyticEntity newEntity = ProductDailyAnalyticEntity.builder()
                        .productId(keyInfo.getProductId())
                        .merchantId(keyInfo.getMerchantId())
                        .periodStart(keyInfo.getPeriodStart())
                        .periodEnd(keyInfo.getPeriodEnd())
                        .viewCount(viewCount)
                        .build();
                toSave.add(newEntity);
            }
        }

        // Save all the entities to the database
        productDailyAnalyticEntityRepository.saveAll(toSave);
        log.info("[TotalVisitsScheduler] Job finished. Updated {} records.", toSave.size());
    }

    /**
     * Class to store information for grouping keys.
     * This class represents a composite key consisting of productId, merchantId, periodStart, and periodEnd.
     */
    @Data
    @AllArgsConstructor
    static class KeyInfo {
        Long productId;
        Long merchantId;
        LocalDate periodStart;
        LocalDate periodEnd;
    }
}





