package org.to2mbn.jmccc.mcdownloader.download.tasks;

import java.util.Objects;

class DownloadTaskCachePoolDecorator<T> extends SimpleDownloadTaskDecorator<T> {

    private String pool;

    public DownloadTaskCachePoolDecorator(DownloadTask<T> delegated, String pool) {
        super(delegated);
        this.pool = Objects.requireNonNull(pool);
    }

    @Override
    public String getCachePool() {
        return pool;
    }
}
