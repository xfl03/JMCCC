package org.to2mbn.jmccc.mcdownloader;

import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask.CacheStrategy;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloader;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.CombinedDownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;
import org.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Version;

import java.util.Objects;
import java.util.concurrent.Future;

class MinecraftDownloaderImpl implements MinecraftDownloader {

    private CombinedDownloader combinedDownloader;
    private MinecraftDownloadProvider downloadProvider;

    public MinecraftDownloaderImpl(CombinedDownloader combinedDownloader, MinecraftDownloadProvider downloadProvider) {
        this.combinedDownloader = Objects.requireNonNull(combinedDownloader);
        this.downloadProvider = Objects.requireNonNull(downloadProvider);
    }

    @Override
    public void shutdown() {
        combinedDownloader.shutdown();
    }

    @Override
    public boolean isShutdown() {
        return combinedDownloader.isShutdown();
    }

    @Override
    public <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback) {
        return combinedDownloader.download(task, callback);
    }

    @Override
    public <T> Future<T> download(CombinedDownloadTask<T> task, CombinedDownloadCallback<T> callback) {
        return combinedDownloader.download(task, callback);
    }

    @Override
    public <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback, int tries) {
        return combinedDownloader.download(task, callback, tries);
    }

    @Override
    public <T> Future<T> download(CombinedDownloadTask<T> task, CombinedDownloadCallback<T> callback, int tries) {
        return combinedDownloader.download(task, callback, tries);
    }

    @Override
    public Future<Version> downloadIncrementally(MinecraftDirectory dir, String version, CombinedDownloadCallback<Version> callback, MinecraftDownloadOption... options) {
        boolean checkLibrariesHash = false;
        boolean checkAssetsHash = false;
        boolean updateSnapshots = false;
        AssetOption assetOption = null;
        CacheOption cacheOption = null;

        for (MinecraftDownloadOption option : options) {
            if (option instanceof CacheOption) {
                cacheOption = (CacheOption) option;

            } else if (option instanceof AssetOption) {
                assetOption = (AssetOption) option;

            } else if (option instanceof MavenOption) {
                switch ((MavenOption) option) {
                    case UPDATE_SNAPSHOTS:
                        updateSnapshots = true;
                        break;
                    default:
                        break;
                }

            } else if (option instanceof ChecksumOption) {
                switch ((ChecksumOption) option) {
                    case CHECK_ASSETS:
                        checkAssetsHash = true;
                        break;
                    case CHECK_LIBRARIES:
                        checkLibrariesHash = true;
                        break;
                    default:
                        break;
                }
            }
        }

        CombinedDownloadTask<Version> task = new IncrementallyDownloadTask(downloadProvider, dir, version, checkLibrariesHash, checkAssetsHash, updateSnapshots, assetOption);

        if (cacheOption != null) {
            task = processCacheOption(task, cacheOption);
        }

        return download(task, callback);
    }

    @Override
    public Future<RemoteVersionList> fetchRemoteVersionList(CombinedDownloadCallback<RemoteVersionList> callback, CacheOption... options) {
        CombinedDownloadTask<RemoteVersionList> task = downloadProvider.versionList();
        if (options.length != 0) {
            // apply the last one
            task = processCacheOption(task, options[options.length - 1]);
        }
        return download(task, callback);
    }

    @Override
    public MinecraftDownloadProvider getProvider() {
        return downloadProvider;
    }

    @Override
    public String toString() {
        return String.format("MinecraftDownloaderImpl [combinedDownloader=%s, downloadProvider=%s]", combinedDownloader, downloadProvider);
    }

    private <T> CombinedDownloadTask<T> processCacheOption(CombinedDownloadTask<T> task, CacheOption option) {
        switch (option) {
            case CACHE:
                return task.cacheable(CacheStrategy.CACHEABLE);
            case FORCIBLY_CACHE:
                return task.cacheable(CacheStrategy.FORCIBLY_CACHE);
            case NO_CACHE:
                return task.cacheable(CacheStrategy.NON_CACHEABLE);
            default:
                return task;
        }
    }

}
