package org.to2mbn.jmccc.mcdownloader;

import java.io.File;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.to2mbn.jmccc.mcdownloader.download.DownloaderService;
import org.to2mbn.jmccc.mcdownloader.download.JdkHttpDownloader;
import org.to2mbn.jmccc.mcdownloader.provider.ExtendedDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.InfoDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.MojangDownloadProvider;

public class MinecraftDownloaderBuilder {

	static final int BIO_MAX_CONNECTIONS = 20;

	public static MinecraftDownloaderBuilder create() {
		return new MinecraftDownloaderBuilder();
	}

	public static MinecraftDownloader buildDefault() {
		return new MinecraftDownloaderBuilder().build();
	}

	int maxConnections;
	int maxConnectionsPerRouter;
	MinecraftDownloadProvider baseProvider = new MojangDownloadProvider();
	List<MinecraftDownloadProvider> appendProviders = new ArrayList<>();
	int poolMaxThreads = Runtime.getRuntime().availableProcessors();
	long poolThreadLivingTime = 1000 * 10; // ms
	int defaultTries = 3;
	int connectTimeout = 10000; // ms
	int soTimeout = 30000;// ms
	boolean disableApacheHttpAsyncClient = false;
	boolean useVersionDownloadInfo = true;
	Proxy proxy = Proxy.NO_PROXY;
	boolean checkLibrariesHash = true;
	boolean checkAssetsHash = true;
	boolean disableBioConnectionsLimit = false;
	boolean disableEhcache = false;
	long cacheLiveTime = 1000 * 60 * 60 * 2; // ms
	long heapCacheSize = 32;// mb
	long offheapCacheSize = 0;// mb
	long diskCacheSize = 0;// mb
	File diskCacheDir;

	protected MinecraftDownloaderBuilder() {
	}

	public MinecraftDownloaderBuilder setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
		return this;
	}

	public MinecraftDownloaderBuilder setMaxConnectionsPerRouter(int maxConnectionsPerRouter) {
		this.maxConnectionsPerRouter = maxConnectionsPerRouter;
		return this;
	}

	public MinecraftDownloaderBuilder setBaseProvider(MinecraftDownloadProvider baseprovider) {
		Objects.requireNonNull(baseprovider);
		this.baseProvider = baseprovider;
		return this;
	}

	public MinecraftDownloaderBuilder setPoolMaxThreads(int poolMaxThreads) {
		this.poolMaxThreads = poolMaxThreads;
		return this;
	}

	public MinecraftDownloaderBuilder setPoolThreadLivingTime(long poolThreadLivingTime) {
		this.poolThreadLivingTime = poolThreadLivingTime;
		return this;
	}

	public MinecraftDownloaderBuilder setDefaultTries(int defaultTries) {
		this.defaultTries = defaultTries;
		return this;
	}

	public MinecraftDownloaderBuilder setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
		return this;
	}

	public MinecraftDownloaderBuilder setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
		return this;
	}

	public MinecraftDownloaderBuilder appendProvider(MinecraftDownloadProvider appendProvider) {
		Objects.requireNonNull(appendProvider);
		appendProviders.add(appendProvider);
		return this;
	}

	public MinecraftDownloaderBuilder disableApacheHttpAsyncClient() {
		disableApacheHttpAsyncClient = true;
		return this;
	}

	public MinecraftDownloaderBuilder disableBioConnectionsLimit() {
		disableBioConnectionsLimit = true;
		return this;
	}

	public MinecraftDownloaderBuilder disableEhcache() {
		disableEhcache = true;
		return this;
	}

	public MinecraftDownloaderBuilder setUseVersionDownloadInfo(boolean useVersionDownloadInfo) {
		this.useVersionDownloadInfo = useVersionDownloadInfo;
		return this;
	}

	public MinecraftDownloaderBuilder setProxy(Proxy proxy) {
		Objects.requireNonNull(proxy);
		this.proxy = proxy;
		return this;
	}

	public MinecraftDownloaderBuilder setCheckLibrariesHash(boolean checkLibrariesHash) {
		this.checkLibrariesHash = checkLibrariesHash;
		return this;
	}

	public MinecraftDownloaderBuilder setCheckAssetsHash(boolean checkAssetsHash) {
		this.checkAssetsHash = checkAssetsHash;
		return this;
	}

	public MinecraftDownloaderBuilder setCacheLiveTime(long cacheLiveTime, TimeUnit timeUnit) {
		this.cacheLiveTime = timeUnit.toMillis(cacheLiveTime);
		return this;
	}

	public MinecraftDownloaderBuilder setHeapCacheSize(long heapCacheSize) {
		this.heapCacheSize = heapCacheSize;
		return this;
	}

	public MinecraftDownloaderBuilder setOffheapCacheSize(long offheapCacheSize) {
		this.offheapCacheSize = offheapCacheSize;
		return this;
	}

	public MinecraftDownloaderBuilder setDiskCacheSize(long diskCacheSize) {
		this.diskCacheSize = diskCacheSize;
		return this;
	}

	public MinecraftDownloaderBuilder setDiskCacheDir(File diskCacheDir) {
		this.diskCacheDir = diskCacheDir;
		return this;
	}

	public MinecraftDownloader build() {
		ExecutorService executor = null;
		DownloaderService downloader = null;
		MinecraftDownloader mcdownloader = null;

		try {
			MinecraftDownloadProvider provider = resolveProvider();

			executor = new ThreadPoolExecutor(poolMaxThreads, poolMaxThreads, poolThreadLivingTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

			if (!disableApacheHttpAsyncClient && isApacheHttpAsyncClientAvailable()) {
				downloader = ApacheHttpAsyncClientFeature.createApacheHttpAsyncClient(executor, this);
			} else {
				int conns = maxConnections > 0 ? maxConnections : Runtime.getRuntime().availableProcessors() * 2;
				if (!disableBioConnectionsLimit)
					conns = Math.min(conns, BIO_MAX_CONNECTIONS);

				downloader = new JdkHttpDownloader(
						conns,
						connectTimeout,
						soTimeout,
						poolThreadLivingTime,
						proxy);
			}

			if (!disableEhcache && isEhcacheAvailable()) {
				downloader = EhcacheFeature.createCachedDownloader(downloader, this);
			}

			mcdownloader = new MinecraftDownloaderImpl(downloader, executor, provider, defaultTries, checkLibrariesHash, checkAssetsHash);
		} catch (Throwable e) {
			if (executor != null) {
				try {
					executor.shutdown();
				} catch (Throwable e1) {
					e.addSuppressed(e1);
				}
			}
			if (downloader != null) {
				try {
					downloader.shutdown();
				} catch (Throwable e1) {
					e.addSuppressed(e1);
				}
			}
			if (mcdownloader != null) {
				try {
					mcdownloader.shutdown();
				} catch (Throwable e1) {
					e.addSuppressed(e1);
				}
			}
			throw e;
		}

		return mcdownloader;
	}

	private static boolean isApacheHttpAsyncClientAvailable() {
		try {
			Class.forName("org.apache.http.impl.nio.client.HttpAsyncClientBuilder");
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}

	private static boolean isEhcacheAvailable() {
		try {
			Class.forName("org.ehcache.config.builders.CacheManagerBuilder");
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}

	/*
	 * Provider Model:
	 * 
	 * <Beginning(p1~p2)> <---------------Mid(p3~p6)------------------------->  <End(p7)>
	 * p1======>p2========>p3=======================>p4=========>p5======>p6======>p7
	 *  |  /|\   |  /|\     |               /||\      |    /||\  ............
	 *  |___|    |___|     \|/               ||      \|/    ||   ............
	 *                    p3.1(same as p1)   ||      p4.1   ||   ............
	 *                      ||               ||       ||    ||   ............
	 *                     \||/              ||      \||/   ||
	 *                    p3.2(same as p2)   ||      p4.2   ||
	 *                     \||/              ||      \||/   ||
	 *                      ||_______________||       ||____||
	 *                      |_________________|       |______|
	 * ====>    parent is
	 * ---->    upstream is
	 * 
	 * In binary tree:
	 * 
	 * Beginning:
	 *                           *
	 *                         /   \
	 *                       p1 =>  *
	 *                            /   \
	 *                          p2 =>  ?(next)
	 * =>    upstream is
	 * 
	 * Full:
	 *     beginning---------\
	 *                        \--------------->*
	 *                                        / \
	 *                                       /   \
	 *                                      /     \
	 *                                     /       \
	 *                                    /         \
	 *                                   /           \
	 *                                  /             \
	 *                                p3 =>beginning-> *
	 *                                                / \
	 *                                               /   \
	 *                                              /     \
	 *                                             /       \
	 *                                            /         \
	 *                                           /           \
	 *                                          /             \
	 *                                        p4 =>beginning-> *
	 *                                                        / \
	 *                                                       /   \
	 *                                                      /     \
	 *                                                     /       \
	 *                                                    /         \
	 *                                                   /           \
	 *                                                  /             \
	 *                                                p5 =>beginning-> *
	 *                                                                / \
	 *                                                               /   \
	 *                                                              /     \
	 *                                                             /       \
	 *                                                            /         \
	 *                                                           /           \
	 *                                                          /             \
	 *                                                        p6 =>beginning-> p7
	 * =>    upstream is
	 * ->    the '?' refers to (see 'Beginning' above)
	 * 
	 * In such a binary tree, all the left trees are leaves. All the right trees(except the right tree in the deepest level) are NOT leaves.
	 * The leaves are DownloadProviders. When resolving download tasks, we first try the left tree, and then the right tree.
	 */

	private MinecraftDownloadProvider resolveProvider() {
		MinecraftDownloadProvider right = baseProvider;
		for (MinecraftDownloadProvider left : appendProviders) {
			if (left instanceof ExtendedDownloadProvider) {
				((ExtendedDownloadProvider) left).setUpstreamProvider(resolveBeginningProvider(right));
			}
			right = new AppendedDownloadProvider(left, right);
		}
		right = resolveBeginningProvider(right);
		return right;
	}

	private MinecraftDownloadProvider resolveBeginningProvider(MinecraftDownloadProvider right) {
		for (MinecraftDownloadProvider left : createBeginningProviders()) {
			if (left instanceof ExtendedDownloadProvider) {
				((ExtendedDownloadProvider) left).setUpstreamProvider(right);
			}
			right = new AppendedDownloadProvider(left, right);
		}
		return right;
	}

	protected List<MinecraftDownloadProvider> createBeginningProviders() {
		List<MinecraftDownloadProvider> providers = new ArrayList<>();
		if (useVersionDownloadInfo)
			providers.add(new InfoDownloadProvider());
		return providers;
	}

}
