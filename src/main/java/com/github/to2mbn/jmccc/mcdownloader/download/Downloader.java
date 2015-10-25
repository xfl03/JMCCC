package com.github.to2mbn.jmccc.mcdownloader.download;

import java.io.IOException;
import java.util.concurrent.Future;

public interface Downloader {

	/**
	 * Submits a download task asynchronously.
	 * 
	 * @param task the download task
	 * @param listener download callback
	 * @return future representing pending completion of the download
	 * @throws NullPointerException <code>task==null</code>
	 * @throws IllegalStateException if the downloader has been shutdown
	 */
	<T> Future<T> download(DownloadTask<T> task, DownloadTaskListener<T> listener);

	/**
	 * Shutdowns the downloader and interrupts all the tasks.
	 * 
	 * @throws IOException if an I/O error occurs during shutdown
	 */
	void shutdown() throws IOException;

	/**
	 * Returns true if this executor has been shutdown.
	 *
	 * @return true if this executor has been shutdown
	 */
	boolean isShutdown();

}
