package org.to2mbn.jmccc.mcdownloader.download.cache;

import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.to2mbn.jmccc.mcdownloader.download.Downloader;
import org.to2mbn.jmccc.mcdownloader.download.cache.provider.CacheProvider;
import org.to2mbn.jmccc.mcdownloader.download.cache.provider.EhcacheProvider;
import org.to2mbn.jmccc.mcdownloader.download.cache.provider.JCacheProvider;
import org.to2mbn.jmccc.util.Builder;
import org.to2mbn.jmccc.util.Builders;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CachedDownloaderBuilder implements Builder<Downloader> {

    private static final Logger LOGGER = Logger.getLogger(CachedDownloaderBuilder.class.getCanonicalName());
    // === Default cache settings
    private static final long DEFAULT_CACHE_TTL = 2;
    private static final TimeUnit DEFAULT_CACHE_TTL_UNIT = TimeUnit.HOURS;
    private static final long DEFAULT_CACHE_HEAP = 32;
    private static final String DEFAULT_CACHE_HEAP_UNIT = "MB";
    protected final Builder<Downloader> underlying;
    protected Builder<? extends CacheProvider<URI, byte[]>> cacheProvider;

    protected CachedDownloaderBuilder(Builder<Downloader> underlying) {
        this.underlying = Objects.requireNonNull(underlying);
    }
    // ===

    public static CachedDownloaderBuilder create(Builder<Downloader> underlying) {
        return new CachedDownloaderBuilder(underlying);
    }

    public static Downloader buildDefault(Builder<Downloader> underlying) {
        return create(underlying).build();
    }

    public static boolean isAvailable() {
        return (JCacheProvider.isAvailable() && JCacheSupport.hasAvailableProvider()) || EhcacheProvider.isAvailable();
    }

    public CachedDownloaderBuilder cacheProvider(Builder<? extends CacheProvider<URI, byte[]>> cacheProvider) {
        this.cacheProvider = Objects.requireNonNull(cacheProvider);
        return this;
    }

    // === Ehcache Supports

    public CachedDownloaderBuilder ehcache(Builder<? extends org.ehcache.CacheManager> ehcache, boolean autoClose) {
        this.cacheProvider = new EhcacheProviderBuilder(Objects.requireNonNull(ehcache), autoClose);
        return this;
    }

    public CachedDownloaderBuilder ehcache(Builder<? extends org.ehcache.CacheManager> ehcache) {
        return ehcache(ehcache, true);
    }

    public CachedDownloaderBuilder ehcache(org.ehcache.config.Builder<? extends org.ehcache.CacheManager> ehcache, boolean autoClose) {
        return ehcache(new EhcacheBuilderAdapter<>(Objects.requireNonNull(ehcache)), autoClose);
    }

    public CachedDownloaderBuilder ehcache(org.ehcache.config.Builder<? extends org.ehcache.CacheManager> ehcache) {
        return ehcache(ehcache, true);
    }

    public CachedDownloaderBuilder ehcache(final org.ehcache.CacheManager ehcache, boolean autoClose) {
        Objects.requireNonNull(ehcache);
        return ehcache(Builders.of(ehcache), autoClose);
    }

    public CachedDownloaderBuilder ehcache(org.ehcache.CacheManager ehcache) {
        return ehcache(ehcache, false);
    }

    public CachedDownloaderBuilder jcache(Builder<? extends javax.cache.CacheManager> jcache, boolean autoClose) {
        this.cacheProvider = new JCacheProviderBuilder(Objects.requireNonNull(jcache), autoClose);
        return this;
    }

    public CachedDownloaderBuilder jcache(Builder<? extends javax.cache.CacheManager> jcache) {
        return jcache(jcache, true);
    }

    public CachedDownloaderBuilder jcache(final javax.cache.CacheManager jcache, boolean autoClose) {
        Objects.requireNonNull(jcache);
        this.cacheProvider = new JCacheProviderBuilder(Builders.of(jcache), autoClose);
        return this;
    }

    // ===

    // === javax.cache Supports

    public CachedDownloaderBuilder jcache(javax.cache.CacheManager jcache) {
        return jcache(jcache, false);
    }

    @Override
    public Downloader build() {
        Downloader underlying = null;
        CacheProvider<URI, byte[]> cache = null;

        try {
            underlying = Objects.requireNonNull(this.underlying.build(), "Underlying downloader builder returns null");
            cache = buildCacheProvider();
            LOGGER.fine("Using cache provider: " + cache);
            return new CachedDownloader(underlying, cache);

        } catch (Throwable e) {
            if (underlying != null) {
                try {
                    underlying.shutdown();
                } catch (Throwable e1) {
                    e.addSuppressed(e1);
                }
            }
            if (cache != null) {
                try {
                    cache.close();
                } catch (Throwable e1) {
                    e.addSuppressed(e1);
                }
            }
            throw e;
        }
    }

    private CacheProvider<URI, byte[]> buildCacheProvider() {
        CacheProvider<URI, byte[]> provider;
        if (cacheProvider == null) {
            provider = createDefaultCacheProvider();
            if (provider == null) {
                throw new IllegalStateException("No default cache provider found");
            }
        } else {
            provider = Objects.requireNonNull(cacheProvider.build(), "Cache provider builder returns null");
        }

        return provider;
    }

    protected CacheProvider<URI, byte[]> createDefaultCacheProvider() {
        CacheProvider<URI, byte[]> provider = null;

        if (JCacheProvider.isAvailable()) {
            provider = JCacheSupport.createDefault();
        }

        if (provider == null) {
            if (EhcacheProvider.isAvailable()) {
                provider = EhcacheSupport.createDefault();
            }
        }

        return provider;
    }

    private static class EhcacheProviderBuilder implements Builder<CacheProvider<URI, byte[]>> {

        private Builder<? extends org.ehcache.CacheManager> ehcache;
        private boolean autoClose;

        public EhcacheProviderBuilder(Builder<? extends org.ehcache.CacheManager> ehcache, boolean autoClose) {
            this.ehcache = ehcache;
            this.autoClose = autoClose;
        }

        @Override
        public CacheProvider<URI, byte[]> build() {
            return EhcacheSupport.adapt(ehcache.build(), autoClose);
        }
    }

    private static class EhcacheBuilderAdapter<T> implements Builder<T> {

        private org.ehcache.config.Builder<T> adapted;

        public EhcacheBuilderAdapter(org.ehcache.config.Builder<T> adapted) {
            this.adapted = adapted;
        }

        @Override
        public T build() {
            return adapted.build();
        }

    }

    // ===

    private static class EhcacheSupport {

        static CacheProvider<URI, byte[]> adapt(org.ehcache.CacheManager manager, boolean autoClose) {
            if (manager.getStatus() == org.ehcache.Status.UNINITIALIZED)
                manager.init();

            return new EhcacheProvider<>(manager, URI.class, byte[].class, autoClose);
        }

        static org.ehcache.config.Builder<org.ehcache.CacheManager> defaultCacheManagerBuilder() {
            return org.ehcache.config.builders.CacheManagerBuilder.newCacheManagerBuilder()
                    .withCache(CacheNames.DEFAULT, CacheConfigurationBuilder.newCacheConfigurationBuilder(URI.class, byte[].class,
                                    ResourcePoolsBuilder.newResourcePoolsBuilder()
                                            .heap(DEFAULT_CACHE_HEAP, MemoryUnit.valueOf(DEFAULT_CACHE_HEAP_UNIT)))
                            .withExpiry(org.ehcache.config.builders.ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(DEFAULT_CACHE_TTL_UNIT.toSeconds(DEFAULT_CACHE_TTL)))));
        }

        static CacheProvider<URI, byte[]> createDefault() {
            return adapt(defaultCacheManagerBuilder().build(), true);
        }

    }

    private static class JCacheProviderBuilder implements Builder<CacheProvider<URI, byte[]>> {

        private Builder<? extends javax.cache.CacheManager> jcache;
        private boolean autoClose;

        public JCacheProviderBuilder(Builder<? extends javax.cache.CacheManager> jcache, boolean autoClose) {
            this.jcache = jcache;
            this.autoClose = autoClose;
        }

        @Override
        public CacheProvider<URI, byte[]> build() {
            return JCacheSupport.adapt(jcache.build(), autoClose);
        }
    }

    private static class JCacheSupport {

        static CacheProvider<URI, byte[]> adapt(javax.cache.CacheManager manager, boolean autoClose) {
            return new JCacheProvider<>(manager, URI.class, byte[].class, autoClose);
        }

        static javax.cache.CacheManager defaultCacheManager() {
            javax.cache.spi.CachingProvider cachingProvider;
            try {
                cachingProvider = javax.cache.Caching.getCachingProvider();
            } catch (javax.cache.CacheException e) {
                return null;
            }
            javax.cache.CacheManager cacheManager = cachingProvider.getCacheManager();

            if (cacheManager.getCache(CacheNames.DEFAULT, URI.class, byte[].class) == null) {
                cacheManager.createCache(CacheNames.DEFAULT, new javax.cache.configuration.MutableConfiguration<URI, byte[]>()
                        .setTypes(URI.class, byte[].class)
                        .setExpiryPolicyFactory(javax.cache.expiry.CreatedExpiryPolicy.factoryOf(
                                new javax.cache.expiry.Duration(DEFAULT_CACHE_TTL_UNIT, DEFAULT_CACHE_TTL))));
            }
            return cacheManager;
        }

        static CacheProvider<URI, byte[]> createDefault() {
            javax.cache.CacheManager cacheManager = defaultCacheManager();
            if (cacheManager == null) {
                return null;
            }
            return adapt(cacheManager, true);
        }

        static boolean hasAvailableProvider() {
            return javax.cache.Caching.getCachingProviders().iterator().hasNext();
        }
    }

}
