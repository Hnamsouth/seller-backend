package com.vtp.vipo.seller.config.cache;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * This class is used to configure the cache properties for the application. It uses Lombok
 * annotations to generate getters and setters for its fields. The fields include the cache type and
 * a map of cache properties. The cache type is an enum that specifies the type of cache to use. The
 * map of cache properties is a map where the key is a string that represents the cache name, and
 * the value is a CacheBuilder object that contains the cache configuration. This class is annotated
 * with @ConfigurationProperties to indicate that its fields should be bound to the environment. The
 * prefix attribute of the @ConfigurationProperties annotation is set to "custom.properties.cache",
 * which means that the fields of this class will be bound to properties that start with this
 * prefix. This class doesn't provide any methods, it only provides fields and their getters and
 * setters. It can be used in combination with other classes to configure and create a cache.
 *
 * @author haidv
 * @version 1.0
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "custom.properties.cache")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CacheProperties {

    CustomCacheProvider inMemory;

    CustomCacheProvider redis;

    @Setter
    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CustomCacheProvider {

        CacheType type;

        Map<String, CacheBuilder> properties;

    }

}
