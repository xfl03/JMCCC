package org.to2mbn.jmccc.mcdownloader.download.cache.provider;

import java.io.Closeable;

public interface CacheProvider<K, V> extends Closeable {

    V get(String cachePool, K key);

    void put(String cachePool, K key, V value);

    void remove(String cachePool, K key);

    boolean hasCache(String cachePool);

}
