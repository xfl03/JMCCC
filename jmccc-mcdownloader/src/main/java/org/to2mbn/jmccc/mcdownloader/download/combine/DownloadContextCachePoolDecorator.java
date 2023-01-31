package org.to2mbn.jmccc.mcdownloader.download.combine;

import org.to2mbn.jmccc.mcdownloader.download.concurrent.CombinedDownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;

import java.util.concurrent.Future;

class DownloadContextCachePoolDecorator<T> extends CombinedDownloadContextDecorator<T> {

    private String cachePool;

    public DownloadContextCachePoolDecorator(CombinedDownloadContext<T> delegated, String cachePool) {
        super(delegated);
        this.cachePool = cachePool;
    }

    @Override
    public <R> Future<R> submit(DownloadTask<R> task, DownloadCallback<R> callback, boolean fatal) throws InterruptedException {
        if (task.getCachePool() == null) {
            task = task.cachePool(cachePool);
        }
        return delegated.submit(task, callback, fatal);
    }

    @Override
    public <R> Future<R> submit(CombinedDownloadTask<R> task, CombinedDownloadCallback<R> callback, boolean fatal) throws InterruptedException {
        if (task.getCachePool() == null) {
            task = task.cachePool(cachePool);
        }
        return delegated.submit(task, callback, fatal);
    }
}
