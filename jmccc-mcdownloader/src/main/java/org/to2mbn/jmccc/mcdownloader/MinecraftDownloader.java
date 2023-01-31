package org.to2mbn.jmccc.mcdownloader;

import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloader;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.CombinedDownloadCallback;
import org.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Version;

import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

public interface MinecraftDownloader extends CombinedDownloader {

    /**
     * Downloads a minecraft version incrementally and asynchronously.
     * <p>
     * The method will check the asset files, library files, game jars. And
     * downloads the missing or broken ones.
     *
     * @param dir      the minecraft dir
     * @param version  the version to download
     * @param callback the callback
     * @param options  options specifying how the minecraft is downloaded
     * @return future representing pending completion of the operation
     * @throws NullPointerException       if <code>dir==null || version==null</code>
     * @throws RejectedExecutionException if the downloader has been shutdown
     */
    Future<Version> downloadIncrementally(MinecraftDirectory dir, String version, CombinedDownloadCallback<Version> callback, MinecraftDownloadOption... options);

    /**
     * Fetches the remote version list asynchronously.
     *
     * @param callback the callback
     * @param options  options specifying how the version list is cached
     * @return future representing pending completion of the operation
     * @throws RejectedExecutionException if the downloader has been shutdown
     */
    Future<RemoteVersionList> fetchRemoteVersionList(CombinedDownloadCallback<RemoteVersionList> callback, CacheOption... options);

    /**
     * Gets the provider of the {@code MinecraftDownloader}.
     *
     * @return the provider of the {@code MinecraftDownloader}
     */
    MinecraftDownloadProvider getProvider();

}
