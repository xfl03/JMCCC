package org.to2mbn.jmccc.mcdownloader.download.cache.ehcache;

import java.net.URI;
import java.util.Objects;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.to2mbn.jmccc.mcdownloader.download.Downloader;
import org.to2mbn.jmccc.mcdownloader.download.cache.AbstractCachedDownloader;

public class EhcacheDownloader extends AbstractCachedDownloader {

	public static final String DEFAULT_CACHE_NAME = EhcacheDownloader.class.getCanonicalName();

	private final Cache<URI, byte[]> defaultCachePool;
	private final CacheManager cacheManager;

	public EhcacheDownloader(Downloader upstream, CacheManager cacheManager) {
		this(upstream, cacheManager, DEFAULT_CACHE_NAME);
	}

	public EhcacheDownloader(Downloader upstream, CacheManager cacheManager, String defaultCacheName) {
		super(upstream);
		Objects.requireNonNull(cacheManager);
		Objects.requireNonNull(defaultCacheName);

		this.cacheManager = cacheManager;
		defaultCachePool = cacheManager.getCache(defaultCacheName, URI.class, byte[].class);
		if (defaultCachePool == null) {
			throw new IllegalArgumentException(String.format("Default cache is not defined [%s]", defaultCacheName));
		}
	}

	@Override
	protected byte[] getCache(String pool, URI key) {
		return getCachePool(pool).get(key);
	}

	@Override
	protected void addCache(String pool, URI key, byte[] value) {
		getCachePool(pool).put(key, value);
	}

	@Override
	protected void removeCache(String pool, URI key) {
		getCachePool(pool).remove(key);
	}

	@Override
	public void shutdown() {
		try {
			super.shutdown();
		} finally {
			cacheManager.close();
		}
	}

	private Cache<URI, byte[]> getCachePool(String poolName) {
		if (poolName == null) {
			return defaultCachePool;
		}
		Cache<URI, byte[]> pool = cacheManager.getCache(poolName, URI.class, byte[].class);
		if (pool == null) {
			return defaultCachePool;
		}
		return pool;
	}

}
