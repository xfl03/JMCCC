package com.github.to2mbn.jmccc.mcdownloader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloaderService;
import com.github.to2mbn.jmccc.mcdownloader.download.HttpAsyncDownloader;
import com.github.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import com.github.to2mbn.jmccc.mcdownloader.provider.MojangDownloadProvider;

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

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}

	public MinecraftDownloader build() {
		ExecutorService executor = new ThreadPoolExecutor(poolCoreThreads, poolMaxThreads, poolThreadLivingTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		HttpAsyncClientBuilder httpClientBuilder = HttpAsyncClientBuilder.create().setMaxConnTotal(maxConnections).setMaxConnPerRoute(maxConnectionsPerRouter).setDefaultIOReactorConfig(IOReactorConfig.custom().setConnectTimeout(connectTimeout).setSoTimeout(soTimeout).build());
		DownloaderService downloader = new HttpAsyncDownloader(httpClientBuilder, executor);
		return new MinecraftDownloaderImpl(downloader, executor, provider, defaultTries);
	}
}
