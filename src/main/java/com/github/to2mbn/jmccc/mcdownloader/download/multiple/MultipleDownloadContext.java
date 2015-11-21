package com.github.to2mbn.jmccc.mcdownloader.download.multiple;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloadCallback;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import com.github.to2mbn.jmccc.mcdownloader.download.concurrent.AsyncCallback;

public interface MultipleDownloadContext<T> extends AsyncCallback<T> {

	Future<?> submit(Runnable task, AsyncCallback<?> callback, boolean fatal) throws InterruptedException;

	<R> Future<R> submit(Callable<R> task, AsyncCallback<R> callback, boolean fatal) throws InterruptedException;

	<R> Future<R> submit(DownloadTask<R> task, DownloadCallback<R> callback, boolean fatal) throws InterruptedException;

	void awaitAllTasks(Runnable callback) throws InterruptedException;

}
