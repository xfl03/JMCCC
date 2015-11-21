package com.github.to2mbn.jmccc.mcdownloader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloadCallback;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloaderService;
import com.github.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloadCallback;
import com.github.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloadTask;
import com.github.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloader;
import com.github.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloaderImpl;
import com.github.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;
import com.github.to2mbn.jmccc.version.Version;

class MinecraftDownloaderImpl implements MinecraftDownloader {

	private DownloaderService downloader;
	private ExecutorService executor;
	private MultipleDownloader multipleDownloader;
	private MinecraftDownloadProvider downloadProvider;
	private int tries;

	private volatile boolean shutdown = false;
	private ReadWriteLock rwlock = new ReentrantReadWriteLock();

	public MinecraftDownloaderImpl(DownloaderService downloader, ExecutorService executor, MinecraftDownloadProvider downloadProvider, int tries) {
		this.downloader = downloader;
		this.executor = executor;
		this.downloadProvider = downloadProvider;
		this.tries = tries;
		multipleDownloader = new MultipleDownloaderImpl(executor, downloader);
	}

	@Override
	public void shutdown() {
		Lock lock = rwlock.writeLock();
		lock.lock();
		try {
			shutdown = true;
			downloader.shutdown();
			executor.shutdownNow();
			downloader = null;
			executor = null;
			multipleDownloader = null;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean isShutdown() {
		return shutdown;
	}

	@Override
	public <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback) {
		Lock lock = rwlock.readLock();
		lock.lock();
		try {
			checkShutdown();
			return downloader.download(task, callback);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback, int tries) {
		Lock lock = rwlock.readLock();
		lock.lock();
		try {
			checkShutdown();
			return downloader.download(task, callback, tries);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public <T> Future<T> download(MultipleDownloadTask<T> task, MultipleDownloadCallback<T> callback, int tries) {
		Lock lock = rwlock.readLock();
		lock.lock();
		try {
			checkShutdown();
			return multipleDownloader.download(task, callback, tries);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Future<Version> downloadIncrementally(MinecraftDirectory dir, String version, MultipleDownloadCallback<Version> callback) {
		return download(new IncrementallyDownloadTask(downloadProvider, dir, version), callback, tries);
	}

	@Override
	public Future<RemoteVersionList> fetchRemoteVersionList(MultipleDownloadCallback<RemoteVersionList> callback) {
		return download(new RemoteVersionListDownloadTask(downloadProvider), callback, tries);
	}

	private void checkShutdown() {
		if (shutdown) {
			throw new RejectedExecutionException("already shutdown");
		}
	}

}
