package org.to2mbn.jmccc.mcdownloader.download;

import org.to2mbn.jmccc.mcdownloader.download.cache.CachedDownloaderBuilder;
import org.to2mbn.jmccc.mcdownloader.download.io.JdkDownloaderBuilder;
import org.to2mbn.jmccc.mcdownloader.download.io.async.HttpAsyncDownloaderBuilder;
import org.to2mbn.jmccc.util.Builder;

public final class DownloaderBuilders {

    private DownloaderBuilders() {
    }

    public static Builder<Downloader> downloader() {
        if (HttpAsyncDownloaderBuilder.isAvailable()) {
            return HttpAsyncDownloaderBuilder.create();
        } else {
            return JdkDownloaderBuilder.create();
        }
    }

    public static Builder<Downloader> cacheableDownloader(Builder<Downloader> underlying) {
        if (CachedDownloaderBuilder.isAvailable()) {
            return CachedDownloaderBuilder.create(underlying);
        } else {
            return underlying;
        }
    }

    public static Builder<Downloader> cacheableDownloader() {
        return cacheableDownloader(downloader());
    }
}
