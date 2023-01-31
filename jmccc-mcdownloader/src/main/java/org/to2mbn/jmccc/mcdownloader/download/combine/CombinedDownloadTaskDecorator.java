package org.to2mbn.jmccc.mcdownloader.download.combine;

import java.util.Objects;

public class CombinedDownloadTaskDecorator<T> extends CombinedDownloadTask<T> {

    protected final CombinedDownloadTask<T> delegated;

    public CombinedDownloadTaskDecorator(CombinedDownloadTask<T> delegated) {
        this.delegated = Objects.requireNonNull(delegated);
    }

    @Override
    public void execute(CombinedDownloadContext<T> context) throws Exception {
        delegated.execute(context);
    }

    @Override
    public CacheStrategy getCacheStrategy() {
        return delegated.getCacheStrategy();
    }

    @Override
    public String getCachePool() {
        return delegated.getCachePool();
    }
}
