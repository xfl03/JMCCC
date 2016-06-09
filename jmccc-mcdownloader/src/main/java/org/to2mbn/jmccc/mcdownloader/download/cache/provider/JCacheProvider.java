package org.to2mbn.jmccc.mcdownloader.download.cache.provider;

import java.io.IOException;
import java.util.Objects;
import javax.cache.Cache;
import javax.cache.CacheManager;
import org.to2mbn.jmccc.mcdownloader.download.cache.CacheProvider;

public class JCacheProvider<K, V> implements CacheProvider<K, V> {

	public static boolean isAvailable() {
		try {
			Class.forName("javax.cache.Caching");
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}

	private CacheManager manager;
	private String defaultPool;
	private boolean closeCache;

	public JCacheProvider(CacheManager manager, String defaultPool, boolean closeCache) {
		this.manager = Objects.requireNonNull(manager);
		this.defaultPool = Objects.requireNonNull(defaultPool);
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
			cache = manager.getCache(pool);
		if (cache == null)
			cache = manager.getCache(defaultPool);
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
		return String.format("JCacheProvider [manager=%s, defaultPool=%s, closeCache=%s]", manager, defaultPool, closeCache);
	}

}
