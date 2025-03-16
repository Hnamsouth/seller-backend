package com.vtp.vipo.seller.config.cache;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

/**
 * This class is used to build a cache configuration. It uses Lombok annotations to generate getters
 * and setters for its fields. The fields include the cache name, the expired time, and the maximum
 * size of the cache. The cache name is a string that identifies the cache. The expired time is a
 * Duration that specifies how long an entry should be kept in the cache before it is automatically
 * removed. The maximum size is an integer that specifies the maximum number of entries the cache
 * can hold. If the maximum size is reached, the cache will automatically remove some entries to
 * make room for new ones. The maximum size is set to Integer.MAX_VALUE by default, which means the
 * cache can hold as many entries as the maximum value of an integer. This class doesn't provide any
 * methods, it only provides fields and their getters and setters. It can be used in combination
 * with other classes to configure and create a cache.
 *
 * @author haidv
 * @version 1.0
 */
@Setter
@Getter
public class CacheBuilder {

    private String cacheName;

    private Duration expiredTime;

    private int maximumSize = Integer.MAX_VALUE;
}
