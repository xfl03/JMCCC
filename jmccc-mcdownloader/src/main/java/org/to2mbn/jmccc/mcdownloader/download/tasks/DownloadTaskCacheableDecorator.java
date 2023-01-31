package org.to2mbn.jmccc.mcdownloader.download.tasks;

class DownloadTaskCacheableDecorator<T> extends SimpleDownloadTaskDecorator<T> {

    private boolean cachable;

    public DownloadTaskCacheableDecorator(DownloadTask<T> delegated, boolean cachable) {
        super(delegated);
        this.cachable = cachable;
    }

    @Override
    public boolean isCacheable() {
        return cachable;
    }

}
