package org.to2mbn.jmccc.mcdownloader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.to2mbn.jmccc.mcdownloader.download.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.DownloaderService;
import org.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloader;
import org.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloaderImpl;
import org.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;
import com.github.to2mbn.jmccc.version.Version;

class MinecraftDownloaderImpl implements MinecraftDownloader {

	private DownloaderService downloader;
	private ExecutorService executor;
	private MultipleDownloader multipleDownloader;
	private MinecraftDownloadProvider downloadProvider;
	private int tries;

	private volatile boolean shutdown = false;
	private ReadWriteLock shutdownLock = new ReentrantReadWriteLock();

	public MinecraftDownloaderImpl(DownloaderService downloader, ExecutorService executor, MinecraftDownloadProvider downloadProvider, int tries) {
		this.downloader = downloader;
		this.executor = executor;
		this.downloadProvider = downloadProvider;
		this.tries = tries;
		multipleDownloader = new MultipleDownloaderImpl(executor, downloader);
	}

	@Override
	public void shutdown() {
		if (!shutdown) {
			Lock lock = shutdownLock.writeLock();
			lock.lock();
			try {
				shutdown = true;
			} finally {
				lock.unlock();
			}

			multipleDownloader.shutdown();
			downloader.shutdown();
			executor.shutdown();
			multipleDownloader = null;
			downloader = null;
			executor = null;
		}
	}

	@Override
	public boolean isShutdown() {
		return shutdown;
	}

	@Override
	public <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback) {
		Lock lock = shutdownLock.readLock();
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
		Lock lock = shutdownLock.readLock();
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
		Lock lock = shutdownLock.readLock();
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
		return download(downloadProvider.versionList(), callback, tries);
	}

	private void checkShutdown() {
		if (shutdown) {
			throw new RejectedExecutionException("already shutdown");
		}
	}

}
