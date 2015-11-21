package com.github.to2mbn.jmccc.mcdownloader;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloaderService;
import com.github.to2mbn.jmccc.mcdownloader.download.HttpAsyncDownloader;
import com.github.to2mbn.jmccc.mcdownloader.provider.JarLibraryDownloadHandler;
import com.github.to2mbn.jmccc.mcdownloader.provider.LibraryDownloadHandler;
import com.github.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import com.github.to2mbn.jmccc.mcdownloader.provider.PackLibraryDownloadHandler;
import com.github.to2mbn.jmccc.mcdownloader.provider.XZPackLibraryDownloadHandler;

public class MinecraftDownloaderBuilder {

	public static MinecraftDownloaderBuilder create() {
		return new MinecraftDownloaderBuilder();
	}

	private Map<String, LibraryDownloadHandler> libraryHandlers = new HashMap<>();
	private int maxConnections = 50;
	private int maxConnectionsPerRouter = 10;
	private MinecraftDownloadProvider provider;
	private int poolMaxThreads = Integer.MAX_VALUE;
	private int poolCoreThreads = 0;
	private long poolThreadLivingTime = 1000 * 60;
	private int defaultTries = 3;

	protected MinecraftDownloaderBuilder() {
		registerLibraryDownloadHandler(".jar", new JarLibraryDownloadHandler());
		registerLibraryDownloadHandler(".pack", new PackLibraryDownloadHandler());
		registerLibraryDownloadHandler(".pack.xz", new XZPackLibraryDownloadHandler());
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

	public void setMaxConnectionsPerRouter(int maxConnectionsPerRouter) {
		this.maxConnectionsPerRouter = maxConnectionsPerRouter;
	}

	public void setProvider(MinecraftDownloadProvider provider) {
		this.provider = provider;
	}

	public void setPoolMaxThreads(int poolMaxThreads) {
		this.poolMaxThreads = poolMaxThreads;
	}

	public void setPoolThreadLivingTime(long poolThreadLivingTime) {
		this.poolThreadLivingTime = poolThreadLivingTime;
	}

	public void setPoolCoreThreads(int poolCoreThreads) {
		this.poolCoreThreads = poolCoreThreads;
	}

	public void setDefaultTries(int defaultTries) {
		this.defaultTries = defaultTries;
	}

	public void registerLibraryDownloadHandler(String postfix, LibraryDownloadHandler handler) {
		libraryHandlers.put(postfix, handler);
	}

	public void unregisterLibraryDownloadHandler(String postfix) {
		libraryHandlers.remove(postfix);
	}

	public MinecraftDownloader build() {
		ProvidedMinecraftDownloadFactory downloadFactory = new ProvidedMinecraftDownloadFactory(provider);
		for (Entry<String, LibraryDownloadHandler> entry : libraryHandlers.entrySet()) {
			downloadFactory.registerLibraryDownloadHandler(entry.getKey(), entry.getValue());
		}
		ExecutorService executor = new ThreadPoolExecutor(poolCoreThreads, poolMaxThreads, poolThreadLivingTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		HttpAsyncClientBuilder httpClientBuilder = HttpAsyncClientBuilder.create().setMaxConnTotal(maxConnections).setMaxConnPerRoute(maxConnectionsPerRouter);
		DownloaderService downloader = new HttpAsyncDownloader(httpClientBuilder, executor);
		return new MinecraftDownloaderImpl(downloader, executor, downloadFactory, defaultTries);
	}
}
