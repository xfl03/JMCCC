package org.to2mbn.jmccc.mcdownloader.download.combine;

class CombinedTaskCachePoolDecorator<T> extends CombinedDownloadTaskDecorator<T> {

    private String cachePool;

    public CombinedTaskCachePoolDecorator(CombinedDownloadTask<T> delegated, String cachePool) {
        super(delegated);
        this.cachePool = cachePool;
    }

    @Override
    public void execute(CombinedDownloadContext<T> context) throws Exception {
        delegated.execute(new DownloadContextCachePoolDecorator<>(context, cachePool));
    }

    @Override
    public String getCachePool() {
        return cachePool;
    }

}
