package org.to2mbn.jmccc.mcdownloader;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.ehcache.impl.config.persistence.CacheManagerPersistenceConfiguration;
import org.to2mbn.jmccc.mcdownloader.download.CachedDownloader;
import org.to2mbn.jmccc.mcdownloader.download.DownloaderService;

final class EhcacheFeature {

	static DownloaderService createCachedDownloader(DownloaderService upstream, MinecraftDownloaderBuilder builder) {
		return new CachedDownloader(upstream, buildCacheManager(builder));
	}

	static CacheManagerBuilder<CacheManager> buildCacheManager(MinecraftDownloaderBuilder builder) {
		CacheManagerBuilder<CacheManager> cacheManagerBuilder = CacheManagerBuilder.newCacheManagerBuilder();

		ResourcePoolsBuilder resourcePoolsBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder();
		if (builder.heapCacheSize > 0) {
			resourcePoolsBuilder = resourcePoolsBuilder.heap(builder.heapCacheSize, MemoryUnit.MB);
		}
		if (builder.offheapCacheSize > 0) {
			resourcePoolsBuilder = resourcePoolsBuilder.offheap(builder.offheapCacheSize, MemoryUnit.MB);
		}
		if (builder.diskCacheSize > 0) {
			if (builder.diskCacheDir == null) {
				throw new IllegalArgumentException("Disk caching is enabled, but cache location is not specified");
			}
			resourcePoolsBuilder = resourcePoolsBuilder.disk(builder.diskCacheSize, MemoryUnit.MB);
			cacheManagerBuilder = cacheManagerBuilder.using(new CacheManagerPersistenceConfiguration(builder.diskCacheDir));
		}

		cacheManagerBuilder = cacheManagerBuilder.withCache(CachedDownloader.DEFAULT_CACHE_NAME,
				CacheConfigurationBuilder.newCacheConfigurationBuilder(URI.class, byte[].class)
						.withResourcePools(resourcePoolsBuilder)
						.withExpiry(Expirations.timeToLiveExpiration(new Duration(builder.cacheLiveTime, TimeUnit.MILLISECONDS))));
		return cacheManagerBuilder;
	}

	private EhcacheFeature() {
	}
}
