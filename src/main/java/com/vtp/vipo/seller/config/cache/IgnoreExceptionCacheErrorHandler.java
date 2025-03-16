package com.vtp.vipo.seller.config.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/**
 * @author haidv
 * @version 1.0
 */
public class IgnoreExceptionCacheErrorHandler implements CacheErrorHandler {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(IgnoreExceptionCacheErrorHandler.class);

  /**
   * Handle the given runtime exception thrown by the cache provider when retrieving an item with
   * the specified {@code key}, possibly rethrowing it as a fatal exception.
   *
   * @param exception the exception thrown by the cache provider
   * @param cache the cache
   * @param key the key used to get the item
   * @see Cache#get(Object)
   */
  @Override
  public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
    LOGGER.error(exception.getMessage(), exception);
  }

  /**
   * Handle the given runtime exception thrown by the cache provider when updating an item with the
   * specified {@code key} and {@code value}, possibly rethrowing it as a fatal exception.
   *
   * @param exception the exception thrown by the cache provider
   * @param cache the cache
   * @param key the key used to update the item
   * @param value the value to associate with the key
   * @see Cache#put(Object, Object)
   */
  @Override
  public void handleCachePutError(
      RuntimeException exception, Cache cache, Object key, Object value) {
    LOGGER.error(exception.getMessage(), exception);
  }

  /**
   * Handle the given runtime exception thrown by the cache provider when clearing an item with the
   * specified {@code key}, possibly rethrowing it as a fatal exception.
   *
   * @param exception the exception thrown by the cache provider
   * @param cache the cache
   * @param key the key used to clear the item
   */
  @Override
  public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
    LOGGER.error(exception.getMessage(), exception);
  }

  /**
   * Handle the given runtime exception thrown by the cache provider when clearing the specified
   * {@link Cache}, possibly rethrowing it as a fatal exception.
   *
   * @param exception the exception thrown by the cache provider
   * @param cache the cache to clear
   */
  @Override
  public void handleCacheClearError(RuntimeException exception, Cache cache) {
    LOGGER.error(exception.getMessage(), exception);
  }
}
