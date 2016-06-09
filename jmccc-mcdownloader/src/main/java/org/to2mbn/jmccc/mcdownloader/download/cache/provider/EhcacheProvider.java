package org.to2mbn.jmccc.mcdownloader.download.cache.provider;

import java.io.IOException;
import java.util.Objects;
import org.ehcache.Cache;
import org.ehcache.CacheManager;

public class EhcacheProvider<K, V> implements CacheProvider<K, V> {

	public static boolean isAvailable() {
		try {
			Class.forName("org.ehcache.config.builders.CacheManagerBuilder");
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}

	private CacheManager manager;
	private String defaultPool;
	private Class<K> keyClass;
	private Class<V> valueClass;
	private boolean closeCache;

	public EhcacheProvider(CacheManager manager, String defaultPool, Class<K> keyClass, Class<V> valueClass, boolean closeCache) {
		this.manager = Objects.requireNonNull(manager);
		this.defaultPool = Objects.requireNonNull(defaultPool);
		this.keyClass = Objects.requireNonNull(keyClass);
		this.valueClass = Objects.requireNonNull(valueClass);
		this.closeCache = closeCache;
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

	private Cache<K, V> getCache(String pool) {
		Cache<K, V> cache = null;
		if (pool != null)
			cache = manager.getCache(pool, keyClass, valueClass);
		if (cache == null)
			cache = manager.getCache(defaultPool, keyClass, valueClass);
		if (cache == null)
			throw new IllegalStateException("Default cache pool [" + defaultPool + "] is not configured.");
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
		return String.format("EhcacheProvider [manager=%s, defaultPool=%s, keyClass=%s, valueClass=%s, closeCache=%s]", manager, defaultPool, keyClass, valueClass, closeCache);
	}

}
