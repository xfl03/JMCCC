package org.to2mbn.jmccc.mcdownloader.download.combine;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.Callback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.CombinedDownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;

class CachePoolCombinedDownloadContext<T> implements CombinedDownloadContext<T> {

	private CombinedDownloadContext<T> proxied;
	private String cachePool;

	public CachePoolCombinedDownloadContext(CombinedDownloadContext<T> proxied, String cachePool) {
		this.proxied = proxied;
		this.cachePool = cachePool;
	}

	@Override
	public void done(T result) {
		proxied.done(result);
	}

	@Override
	public void failed(Throwable e) {
		proxied.failed(e);
	}

	@Override
	public void cancelled() {
		proxied.cancelled();
	}

	@Override
	public <R> Future<R> submit(Callable<R> task, Callback<R> callback, boolean fatal) throws InterruptedException {
		return proxied.submit(task, callback, fatal);
	}

	@Override
	public void awaitAllTasks(Callable<Void> callback) throws InterruptedException {
		proxied.awaitAllTasks(callback);
	}

	@Override
	public <R> Future<R> submit(DownloadTask<R> task, DownloadCallback<R> callback, boolean fatal) throws InterruptedException {
		if (task.getCachePool() == null) {
			task = task.cachePool(cachePool);
		}
		return proxied.submit(task, callback, fatal);
	}

	@Override
	public <R> Future<R> submit(CombinedDownloadTask<R> task, CombinedDownloadCallback<R> callback, boolean fatal) throws InterruptedException {
		if (task.getCachePool() == null) {
			task = task.cachePool(cachePool);
		}
		return proxied.submit(task, callback, fatal);
	}

}
