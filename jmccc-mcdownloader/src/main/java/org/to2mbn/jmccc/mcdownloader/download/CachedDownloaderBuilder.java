package org.to2mbn.jmccc.mcdownloader.download;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.to2mbn.jmccc.util.Builder;

public class CachedDownloaderBuilder implements Builder<Downloader> {

	public static boolean isAvailable() {
		try {
			Class.forName("org.ehcache.config.builders.CacheManagerBuilder");
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}

	private static class EhcacheSupport {

		// If we write the following code in CachedDownloaderBuilder
		// The fucking JVM will try to load ResourceUnit when loading CachedDownloaderBuilder
		// Then a NoClassDefFoundError will be thrown
		// So we must move it into another class
		static CacheManager newDefaultCacheManager(String cacheName) {
			return CacheManagerBuilder.newCacheManagerBuilder()
					.withCache(cacheName, CacheConfigurationBuilder.newCacheConfigurationBuilder(URI.class, byte[].class)
							.withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder()
									.heap(DEFAULT_CACHE_HEAP, MemoryUnit.valueOf(DEFAULT_CACHE_HEAP_UNIT)))
							.withExpiry(Expirations.timeToLiveExpiration(new Duration(DEFAULT_CACHE_TTL, DEFAULT_CACHE_TTL_UNIT))))
					.build();
		}
	}

	private static final long DEFAULT_CACHE_TTL = 2;
	private static final TimeUnit DEFAULT_CACHE_TTL_UNIT = TimeUnit.HOURS;
	private static final long DEFAULT_CACHE_HEAP = 32;
	private static final String DEFAULT_CACHE_HEAP_UNIT = "MB";

	public static CachedDownloaderBuilder create() {
		return new CachedDownloaderBuilder();
	}

	public static Downloader buildDefault() {
		return create().build();
	}

	protected Builder<Downloader> underlying;
	protected Builder<CacheManager> cacheManager;
	protected String cacheName;

	protected CachedDownloaderBuilder() {}

	public CachedDownloaderBuilder underlying(Builder<Downloader> underlying) {
		this.underlying = underlying;
		return this;
	}

	public CachedDownloaderBuilder cacheManager(Builder<CacheManager> cacheManager) {
		this.cacheManager = cacheManager;
		return this;
	}

	public CachedDownloaderBuilder cacheName(String cacheName) {
		this.cacheName = cacheName;
		return this;
	}

	@Override
	public Downloader build() {
		if (underlying == null) {
			throw new IllegalArgumentException("No underlying DownloaderService");
		}

		String cacheName = this.cacheName == null ? CachedDownloader.DEFAULT_CACHE_NAME : this.cacheName;

		CacheManager cacheManager = null;
		Downloader underlying = null;
		try {
			cacheManager = this.cacheManager == null ? buildDefaultCacheManager() : this.cacheManager.build();
			underlying = this.underlying.build();
			return new CachedDownloader(underlying, cacheManager, cacheName);
		} catch (Throwable e) {
			if (cacheManager != null) {
				try {
					cacheManager.close();
				} catch (Throwable e1) {
					e.addSuppressed(e1);
				}
			}
			if (underlying != null) {
				try {
					underlying.shutdown();
				} catch (Throwable e1) {
					e.addSuppressed(e1);
				}
			}
			throw e;
		}
	}

	protected CacheManager buildDefaultCacheManager() {
		return EhcacheSupport.newDefaultCacheManager(cacheName);
	}

}
