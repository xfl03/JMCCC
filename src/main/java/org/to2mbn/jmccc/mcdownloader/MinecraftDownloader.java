package org.to2mbn.jmccc.mcdownloader;

import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import org.to2mbn.jmccc.mcdownloader.download.DownloaderService;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.Shutdownable;
import org.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloader;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;
import com.github.to2mbn.jmccc.version.Version;

public interface MinecraftDownloader extends Shutdownable, DownloaderService, MultipleDownloader {

	/**
	 * Downloads a minecraft version incrementally and asynchronously.
	 * <p>
	 * This method checks assets, libraries, game jars. And downloads the missing or broken files.
	 * 
	 * @param dir the minecraft dir
	 * @param version the version to download
	 * @param callback the callback
	 * @return future representing pending completion of the operation
	 * @throws NullPointerException if <code>dir==null || version==null</code>
	 * @throws RejectedExecutionException if the downloader has been shutdown
	 */
	Future<Version> downloadIncrementally(MinecraftDirectory dir, String version, MultipleDownloadCallback<Version> callback);

	/**
	 * Fetches the remote version list asynchronously.
	 * 
	 * @param callback the callback
	 * @return future representing pending completion of the operation
	 * @throws RejectedExecutionException if the downloader has been shutdown
	 */
	Future<RemoteVersionList> fetchRemoteVersionList(MultipleDownloadCallback<RemoteVersionList> callback);

}
