package org.to2mbn.jmccc.mcdownloader.download.cache.provider;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.io.IOException;
import java.util.Objects;

public class JCacheProvider<K, V> implements CacheProvider<K, V> {

    private CacheManager manager;
    private Class<K> keyClass;
    private Class<V> valueClass;
    private boolean closeCache;
    public JCacheProvider(CacheManager manager, Class<K> keyClass, Class<V> valueClass, boolean closeCache) {
        this.manager = Objects.requireNonNull(manager);
        this.keyClass = Objects.requireNonNull(keyClass);
        this.valueClass = Objects.requireNonNull(valueClass);
        this.closeCache = closeCache;
    }

    public static boolean isAvailable() {
        try {
            Class.forName("javax.cache.Caching");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public V get(String cachePool, K key) {
        return getCache(cachePool).get(key);
    }

    @Override
    public void put(String cachePool, K key, V value) {
        getCache(cachePool).put(key, value);
    }

    @Override
    public void remove(String cachePool, K key) {
        getCache(cachePool).remove(key);
    }

    @Override
    public boolean hasCache(String cachePool) {
        return manager.getCache(cachePool, keyClass, valueClass) != null;
    }

    private Cache<K, V> getCache(String pool) {
        Cache<K, V> cache = manager.getCache(pool, keyClass, valueClass);
        if (cache == null)
            throw new IllegalStateException("Default cache pool [" + pool + "] is not configured.");
        return cache;
    }

    @Override
    public void close() throws IOException {
        if (closeCache) {
            manager.close();
        }
    }

    @Override
    public String toString() {
        return String.format("JCacheProvider [manager=%s, keyClass=%s, valueClass=%s, closeCache=%s]", manager, keyClass, valueClass, closeCache);
    }

}
