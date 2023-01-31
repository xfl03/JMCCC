package org.to2mbn.jmccc.mcdownloader.download.combine;

class CombinedTaskCacheStrategyDecorator<T> extends CombinedDownloadTaskDecorator<T> {

    private CacheStrategy strategy;

    public CombinedTaskCacheStrategyDecorator(CombinedDownloadTask<T> delegated, CacheStrategy strategy) {
        super(delegated);
        this.strategy = strategy;
    }

    @Override
    public void execute(CombinedDownloadContext<T> context) throws Exception {
        delegated.execute(new DownloadContextCacheStrategyDecorator<>(context, strategy));
    }

    @Override
    public CacheStrategy getCacheStrategy() {
        return strategy;
    }

}
