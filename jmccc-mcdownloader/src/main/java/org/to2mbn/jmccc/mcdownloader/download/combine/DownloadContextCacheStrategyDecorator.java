package org.to2mbn.jmccc.mcdownloader.download.combine;

import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask.CacheStrategy;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.CombinedDownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;

import java.util.concurrent.Future;

class DownloadContextCacheStrategyDecorator<T> extends CombinedDownloadContextDecorator<T> {

    private CacheStrategy strategy;

    public DownloadContextCacheStrategyDecorator(CombinedDownloadContext<T> delegated, CacheStrategy strategy) {
        super(delegated);
        this.strategy = strategy;
    }

    @Override
    public <R> Future<R> submit(DownloadTask<R> task, DownloadCallback<R> callback, boolean fatal) throws InterruptedException {
        DownloadTask<R> processed;
        switch (strategy) {
            case CACHEABLE:
            case FORCIBLY_CACHE:
                processed = task.cacheable(true);
                break;

            case NON_CACHEABLE:
                processed = task.cacheable(false);
                break;

            default:
                processed = task;
                break;
        }
        return delegated.submit(processed, callback, fatal);
    }

    @Override
    public <R> Future<R> submit(CombinedDownloadTask<R> task, CombinedDownloadCallback<R> callback, boolean fatal) throws InterruptedException {
        CombinedDownloadTask<R> processed;
        switch (strategy) {
            case CACHEABLE:
                if (task.getCacheStrategy() == CacheStrategy.DEFAULT) {
                    processed = task.cacheable(CacheStrategy.CACHEABLE);
                } else {
                    processed = task;
                }
                break;

            case FORCIBLY_CACHE:
            case NON_CACHEABLE:
                processed = task.cacheable(strategy);
                break;

            default:
                processed = task;
                break;
        }
        return delegated.submit(processed, callback, fatal);
    }
}
