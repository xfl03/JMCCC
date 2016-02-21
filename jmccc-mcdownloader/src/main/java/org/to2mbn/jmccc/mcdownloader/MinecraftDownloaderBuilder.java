package org.to2mbn.jmccc.mcdownloader;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.to2mbn.jmccc.mcdownloader.download.DownloaderService;
import org.to2mbn.jmccc.mcdownloader.download.HttpAsyncDownloader;
import org.to2mbn.jmccc.mcdownloader.download.JreHttpDownloader;
import org.to2mbn.jmccc.mcdownloader.provider.ExtendedDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.InfoDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.MojangDownloadProvider;

public class MinecraftDownloaderBuilder {

	public static MinecraftDownloaderBuilder create() {
		return new MinecraftDownloaderBuilder();
	}

	private int maxConnections = 50;
	private int maxConnectionsPerRouter = 10;
	private MinecraftDownloadProvider baseProvider = new MojangDownloadProvider();
	private List<MinecraftDownloadProvider> appendProviders = new ArrayList<>();
	private int poolMaxThreads = Runtime.getRuntime().availableProcessors();
	private long poolThreadLivingTime = 1000 * 10;
	private int defaultTries = 3;
	private int connectTimeout = 10000;
	private int soTimeout = 30000;
	private boolean disableApacheHttpAsyncClient = false;
	private boolean useVersionDownloadInfo = true;
	private Proxy proxy = Proxy.NO_PROXY;
	private boolean checkLibrariesHash = true;
	private boolean checkAssetsHash = true;

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

	public MinecraftDownloader build() {
		ExecutorService executor = null;
		DownloaderService downloader = null;
		MinecraftDownloader mcdownloader = null;

		try {
			MinecraftDownloadProvider provider = resolveProvider();

			executor = new ThreadPoolExecutor(poolMaxThreads, poolMaxThreads, poolThreadLivingTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

			if (!disableApacheHttpAsyncClient && isApacheHttpAsyncClientAvailable()) {
				HttpHost proxyHost = resolveProxy();
				HttpAsyncClientBuilder httpClientBuilder = HttpAsyncClientBuilder.create()
						.setMaxConnTotal(maxConnections)
						.setMaxConnPerRoute(maxConnectionsPerRouter)
						.setProxy(proxyHost)
						.setDefaultIOReactorConfig(IOReactorConfig.custom()
								.setConnectTimeout(connectTimeout)
								.setSoTimeout(soTimeout)
								.build())
						.setDefaultRequestConfig(RequestConfig.custom()
								.setConnectTimeout(connectTimeout)
								.setSocketTimeout(soTimeout)
								.setProxy(proxyHost)
								.build());
				downloader = new HttpAsyncDownloader(httpClientBuilder, executor);
			} else {
				downloader = new JreHttpDownloader(maxConnections, connectTimeout, soTimeout, poolThreadLivingTime, proxy);
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

	private HttpHost resolveProxy() {
		if (proxy.type() == Proxy.Type.DIRECT) {
			return null;
		}
		if (proxy.type() == Proxy.Type.HTTP) {
			SocketAddress socketAddress = proxy.address();
			if (socketAddress instanceof InetSocketAddress) {
				InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
				return new HttpHost(inetSocketAddress.getAddress(), inetSocketAddress.getPort());
			}
		}
		throw new IllegalArgumentException("Proxy '" + proxy + "' is not supported");
	}

	private static boolean isApacheHttpAsyncClientAvailable() {
		try {
			Class.forName("org.apache.http.impl.nio.client.HttpAsyncClientBuilder");
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
