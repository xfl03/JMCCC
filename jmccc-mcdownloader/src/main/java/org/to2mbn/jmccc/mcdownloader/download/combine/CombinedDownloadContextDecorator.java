package org.to2mbn.jmccc.mcdownloader.download.combine;

import org.to2mbn.jmccc.mcdownloader.download.concurrent.Callback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.CombinedDownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class CombinedDownloadContextDecorator<T> implements CombinedDownloadContext<T> {

    protected final CombinedDownloadContext<T> delegated;

    public CombinedDownloadContextDecorator(CombinedDownloadContext<T> delegated) {
        this.delegated = Objects.requireNonNull(delegated);
    }

    @Override
    public void done(T result) {
        delegated.done(result);
    }

    @Override
    public void failed(Throwable e) {
        delegated.failed(e);
    }

    @Override
    public void cancelled() {
        delegated.cancelled();
    }

    @Override
    public <R> Future<R> submit(Callable<R> task, Callback<R> callback, boolean fatal) throws InterruptedException {
        return delegated.submit(task, callback, fatal);
    }

    @Override
    public <R> Future<R> submit(DownloadTask<R> task, DownloadCallback<R> callback, boolean fatal) throws InterruptedException {
        return delegated.submit(task, callback, fatal);
    }

    @Override
    public <R> Future<R> submit(CombinedDownloadTask<R> task, CombinedDownloadCallback<R> callback, boolean fatal) throws InterruptedException {
        return delegated.submit(task, callback, fatal);
    }

    @Override
    public void awaitAllTasks(Callable<Void> callback) throws InterruptedException {
        delegated.awaitAllTasks(callback);
    }

}
