package org.researchedc.dao.cache;

import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EhCacheWrapper<K, V> implements CacheWrapper<K, V> {
    private final String cacheName;
    private final Cache<K, V> cache;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public EhCacheWrapper(final String cacheName, final Cache<K, V> cache) {
        this.cacheName = cacheName;
        this.cache = cache;
    }

    @Override
    public void put(final K key, final V value) {
        cache.put(key, value);
    }

    @Override
    public V get(final K key) {
        V value = cache.getIfPresent(key);
        logger.debug("Cache get for key={}, hit={}", key, value != null);
        return value;
    }

    public String getCacheName() {
        return cacheName;
    }

    public Cache<K, V> getCache() {
        return cache;
    }
}
