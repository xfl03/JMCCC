package com.github.to2mbn.jmccc.mcdownloader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloaderService;
import com.github.to2mbn.jmccc.mcdownloader.download.HttpAsyncDownloader;
import com.github.to2mbn.jmccc.mcdownloader.provider.JarLibraryDownloadHandler;
import com.github.to2mbn.jmccc.mcdownloader.provider.LibraryDownloadHandler;
import com.github.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import com.github.to2mbn.jmccc.mcdownloader.provider.MojangDownloadProvider;
import com.github.to2mbn.jmccc.mcdownloader.provider.PackLibraryDownloadHandler;
import com.github.to2mbn.jmccc.mcdownloader.provider.XZPackLibraryDownloadHandler;

public class MinecraftDownloaderBuilder {

	public static MinecraftDownloaderBuilder create() {
		return new MinecraftDownloaderBuilder();
	}

	private Map<String, LibraryDownloadHandler> libraryHandlers = new HashMap<>();
	private Set<MinecraftDownloadProvider> extraProviders = new HashSet<>();
	private int maxConnections = 50;
	private int maxConnectionsPerRouter = 10;
	private MinecraftDownloadProvider provider;
	private int poolMaxThreads = Integer.MAX_VALUE;
	private int poolCoreThreads = 0;
	private long poolThreadLivingTime = 1000 * 60;
	private int defaultTries = 3;
	private int connectTimeout = 10000;
	private int soTimeout = 30000;

	protected MinecraftDownloaderBuilder() {
		registerLibraryDownloadHandler(".jar", new JarLibraryDownloadHandler());
		registerLibraryDownloadHandler(".pack", new PackLibraryDownloadHandler());
		registerLibraryDownloadHandler(".pack.xz", new XZPackLibraryDownloadHandler());
		provider = new MojangDownloadProvider();
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

	public MinecraftDownloaderBuilder registerExtraProvider(MinecraftDownloadProvider provider) {
		extraProviders.add(provider);
		return this;
	}

	public MinecraftDownloaderBuilder unregisterExtraProvider(MinecraftDownloadProvider provider) {
		extraProviders.remove(provider);
		return this;
	}

	public MinecraftDownloaderBuilder registerLibraryDownloadHandler(String postfix, LibraryDownloadHandler handler) {
		libraryHandlers.put(postfix, handler);
		return this;
	}

	public MinecraftDownloaderBuilder unregisterLibraryDownloadHandler(String postfix) {
		libraryHandlers.remove(postfix);
		return this;
	}

	public MinecraftDownloader build() {
		ProvidedMinecraftDownloadFactory downloadFactory = new ProvidedMinecraftDownloadFactory(provider);
		for (Entry<String, LibraryDownloadHandler> entry : libraryHandlers.entrySet()) {
			downloadFactory.registerLibraryDownloadHandler(entry.getKey(), entry.getValue());
		}
		for (MinecraftDownloadProvider extraProvider : extraProviders) {
			downloadFactory.registerExtraProvider(extraProvider);
		}
		ExecutorService executor = new ThreadPoolExecutor(poolCoreThreads, poolMaxThreads, poolThreadLivingTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		HttpAsyncClientBuilder httpClientBuilder = HttpAsyncClientBuilder.create().setMaxConnTotal(maxConnections).setMaxConnPerRoute(maxConnectionsPerRouter).setDefaultIOReactorConfig(IOReactorConfig.custom().setConnectTimeout(connectTimeout).setSoTimeout(soTimeout).build());
		DownloaderService downloader = new HttpAsyncDownloader(httpClientBuilder, executor);
		return new MinecraftDownloaderImpl(downloader, executor, downloadFactory, defaultTries);
	}
}
