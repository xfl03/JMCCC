package com.github.to2mbn.jmccc.mcdownloader.download;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

public interface Downloader {

	/**
	 * Submits a download task asynchronously.
	 * <p>
	 * The task won't be retried if the download failed.
	 * 
	 * @param task the download task
	 * @param listener download callback
	 * @return future representing pending completion of the download
	 * @throws NullPointerException <code>task==null</code>
	 * @throws RejectedExecutionException if the downloader has been shutdown
	 */
	<T> Future<T> download(DownloadTask<T> task, DownloadTaskListener<T> listener);

	/**
	 * Submits a download task asynchronously.
	 * <p>
	 * Download will be retried only when an <code>IOException</code> occurs. If <code>retries==0</code>, the download
	 * won't be retry at all (retry 0 times).
	 * 
	 * @param task the download task
	 * @param listener download callback
	 * @param retries the retry count
	 * @return future representing pending completion of the download
	 * @throws NullPointerException <code>task==null</code>
	 * @throws RejectedExecutionException if the downloader has been shutdown
	 * @throws IllegalArgumentException if <code>retries==null</code>
	 */
	<T> Future<T> download(DownloadTask<T> task, DownloadTaskListener<T> listener, int retries);

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
