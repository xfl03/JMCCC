package com.github.to2mbn.jmccc.mcdownloader;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloaderService;
import com.github.to2mbn.jmccc.mcdownloader.download.HttpAsyncDownloader;
import com.github.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import com.github.to2mbn.jmccc.mcdownloader.provider.MojangDownloadProvider;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;
import com.github.to2mbn.jmccc.version.Asset;
import com.github.to2mbn.jmccc.version.Library;

public class MinecraftDownloaderBuilder {

	public static MinecraftDownloaderBuilder create() {
		return new MinecraftDownloaderBuilder();
	}

	private int maxConnections = 50;
	private int maxConnectionsPerRouter = 10;
	private MinecraftDownloadProvider provider = new MojangDownloadProvider();
	private int poolMaxThreads = Integer.MAX_VALUE;
	private int poolCoreThreads = 0;
	private long poolThreadLivingTime = 1000 * 60;
	private int defaultTries = 3;
	private int connectTimeout = 10000;
	private int soTimeout = 30000;

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

	public MinecraftDownloaderBuilder setProvider(MinecraftDownloadProvider provider) {
		this.provider = provider;
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

	public MinecraftDownloaderBuilder setPoolCoreThreads(int poolCoreThreads) {
		this.poolCoreThreads = poolCoreThreads;
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

	public MinecraftDownloaderBuilder appendProvider(final MinecraftDownloadProvider appendprovider) {
		final MinecraftDownloadProvider prevprovider = provider;
		provider = new MinecraftDownloadProvider() {

			@Override
			public DownloadTask<RemoteVersionList> versionList() {
				DownloadTask<RemoteVersionList> t = appendprovider.versionList();
				if (t == null) {
					return prevprovider.versionList();
				}
				return t;
			}

			@Override
			public DownloadTask<Object> library(MinecraftDirectory mcdir, Library library) {
				DownloadTask<Object> t = appendprovider.library(mcdir, library);
				if (t == null) {
					return prevprovider.library(mcdir, library);
				}
				return t;
			}

			@Override
			public DownloadTask<Object> gameVersionJson(MinecraftDirectory mcdir, String version) {
				DownloadTask<Object> t = appendprovider.gameVersionJson(mcdir, version);
				if (t == null) {
					return prevprovider.gameVersionJson(mcdir, version);
				}
				return t;
			}

			@Override
			public DownloadTask<Object> gameJar(MinecraftDirectory mcdir, String version) {
				DownloadTask<Object> t = appendprovider.gameJar(mcdir, version);
				if (t == null) {
					return prevprovider.gameJar(mcdir, version);
				}
				return t;
			}

			@Override
			public DownloadTask<Set<Asset>> assetsIndex(MinecraftDirectory mcdir, String version) {
				DownloadTask<Set<Asset>> t = appendprovider.assetsIndex(mcdir, version);
				if (t == null) {
					return prevprovider.assetsIndex(mcdir, version);
				}
				return t;
			}

			@Override
			public DownloadTask<Object> asset(MinecraftDirectory mcdir, Asset asset) {
				DownloadTask<Object> t = appendprovider.asset(mcdir, asset);
				if (t == null) {
					return prevprovider.asset(mcdir, asset);
				}
				return t;
			}
		};
		return this;
	}

	public MinecraftDownloader build() {
		ExecutorService executor = new ThreadPoolExecutor(poolCoreThreads, poolMaxThreads, poolThreadLivingTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		HttpAsyncClientBuilder httpClientBuilder = HttpAsyncClientBuilder.create().setMaxConnTotal(maxConnections).setMaxConnPerRoute(maxConnectionsPerRouter).setDefaultIOReactorConfig(IOReactorConfig.custom().setConnectTimeout(connectTimeout).setSoTimeout(soTimeout).build());
		DownloaderService downloader = new HttpAsyncDownloader(httpClientBuilder, executor);
		return new MinecraftDownloaderImpl(downloader, executor, provider, defaultTries);
	}
}
