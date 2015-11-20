package com.github.to2mbn.jmccc.mcdownloader;

import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import com.github.to2mbn.jmccc.mcdownloader.download.concurrent.Shutdownable;
import com.github.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloadCallback;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;

public interface MinecraftDownloader extends Shutdownable {

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
	Future<Object> downloadIncrementally(MinecraftDirectory dir, String version, MultipleDownloadCallback<Object> callback);

	/**
	 * Fetches the remote version list asynchronously.
	 * 
	 * @param callback the callback
	 * @return future representing pending completion of the operation
	 * @throws RejectedExecutionException if the downloader has been shutdown
	 */
	Future<RemoteVersionList> fetchRemoteVersionList(MultipleDownloadCallback<RemoteVersionList> callback);

}
