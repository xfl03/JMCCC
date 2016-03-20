package org.to2mbn.jmccc.mcdownloader.download.combine;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.to2mbn.jmccc.mcdownloader.download.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.ResultProcessor;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.Callback;

class AppendedCombinedDownloadContext<R, S> implements CombinedDownloadContext<R> {

	ResultProcessor<R, S> processor;
	CombinedDownloadContext<S> proxied;

	AppendedCombinedDownloadContext(ResultProcessor<R, S> processor, CombinedDownloadContext<S> proxied) {
		this.processor = processor;
		this.proxied = proxied;
	}

	@Override
	public void done(R result) {
		try {
			proxied.done(processor.process(result));
		} catch (Throwable e) {
			proxied.failed(e);
		}
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
	public <U> Future<U> submit(Callable<U> task, Callback<U> callback, boolean fatal) throws InterruptedException {
		return proxied.submit(task, callback, fatal);
	}

	@Override
	public <U> Future<U> submit(DownloadTask<U> task, DownloadCallback<U> callback, boolean fatal) throws InterruptedException {
		return proxied.submit(task, callback, fatal);
	}
	@Override
	public <U> Future<U> submit(CombinedDownloadTask<U> task, CombinedDownloadCallback<U> callback, boolean fatal) throws InterruptedException {
		return proxied.submit(task, callback, fatal);
	}

	@Override
	public void awaitAllTasks(Callable<Void> callback) throws InterruptedException {
		proxied.awaitAllTasks(callback);
	}


}