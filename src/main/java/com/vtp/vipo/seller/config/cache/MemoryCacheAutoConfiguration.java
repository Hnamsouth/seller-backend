package com.vtp.vipo.seller.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;

/**
 * This configuration class is used to configure the memory cache for the application. It uses
 * the @Configuration annotation to indicate that it is a configuration class.
 * The @ConditionalOnProperty annotation is used to specify that this configuration should only be
 * applied if the cache type is set to "caffeine" in the properties. The @Import annotation is used
 * to import the CacheAutoConfiguration class, which provides additional cache configuration. This
 * class defines several beans that are used to configure and create the cache. The ticker bean is a
 * system ticker that can be used to measure time for the cache. The cacheManager bean is a cache
 * manager that manages the cache. The buildCache method is a private method that is used to build a
 * cache with the specified cache builder and ticker.
 *
 * @author haidv
 * @version 1.0
 */
@Configuration(proxyBeanMethods = false)
@Import(CacheAutoConfiguration.class)
public class MemoryCacheAutoConfiguration {

    /**
     * This bean provides a system ticker that can be used to measure time for the cache. It uses
     * the @Bean annotation to indicate that it is a bean.
     *
     * @return a system ticker
     */
    @Bean
    public Ticker ticker() {
        return Ticker.systemTicker();
    }

    /**
     * This bean provides a cache manager that manages the cache. It uses the @Bean annotation to
     * indicate that it is a bean. The @ConditionalOnMissingBean annotation is used to specify that
     * this bean should only be created if there is no other bean of the same type in the context.
     * The @ConditionalOnProperty annotation is used to specify that this bean should only be created
     * if the cache type is set to "caffeine" in the properties.
     *
     * @param ticker          the system ticker
     * @param cacheProperties the cache properties
     * @return a cache manager
     */
    @Primary
    @ConditionalOnProperty(
            name = "custom.properties.cache.in-memory.type",
            havingValue = "CAFFEINE"
    )
    @ConditionalOnMissingBean
    @Bean("memoryCacheManager")
    public SimpleCacheManager memoryCacheManager(final Ticker ticker, final CacheProperties cacheProperties) {
        final List<CaffeineCache> caches = new ArrayList<>();
        cacheProperties.getInMemory()
                .getProperties()
                .forEach(
                        (k, v) -> {
                            CaffeineCache cache = this.buildCache(v, ticker);
                            caches.add(cache);
                        });
        final SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(caches);
        return manager;
    }

    /**
     * This method is used to build a cache with the specified cache builder and ticker. It is a
     * private method, so it can only be used within this class.
     *
     * @param cacheBuilder the cache builder
     * @param ticker       the system ticker
     * @return a caffeine cache
     */
    private CaffeineCache buildCache(final CacheBuilder cacheBuilder, final Ticker ticker) {
        return new CaffeineCache(
                cacheBuilder.getCacheName(),
                Caffeine.newBuilder()
                        .expireAfterWrite(cacheBuilder.getExpiredTime())
                        .maximumSize(cacheBuilder.getMaximumSize())
                        .ticker(ticker)
                        .build());
    }
}
