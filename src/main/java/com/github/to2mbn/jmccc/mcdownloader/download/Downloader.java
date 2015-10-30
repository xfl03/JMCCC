package com.github.to2mbn.jmccc.mcdownloader.download;

import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import com.github.to2mbn.jmccc.mcdownloader.download.concurrent.Shutdownable;

public interface Downloader extends Shutdownable {

	/**
	 * Submits a download task asynchronously.
	 * <p>
	 * The task won't be retried if the download failed.
	 * 
	 * @param task the download task
	 * @param listener download callback
	 * @return future representing pending completion of the download
	 * @throws NullPointerException <code>task == null</code>
	 * @throws RejectedExecutionException if the downloader has been shutdown
	 */
	<T> Future<T> download(DownloadTask<T> task, DownloadTaskListener<T> listener);

	/**
	 * Submits a download task asynchronously.
	 * <p>
	 * Download will be retried only when an <code>IOException</code> occurs.<br>
	 * <code>tries</code> is the max number of tries. For example: If <code>tries==1</code>, the download won't be retry
	 * (try 1 time, retry 0 time); If <code>tries==5</code>, the download be retry at most 4 times.
	 * 
	 * @param task the download task
	 * @param listener download callback
	 * @param tries the max number of tries
	 * @return future representing pending completion of the download
	 * @throws NullPointerException <code>task == null</code>
	 * @throws IllegalArgumentException if <code>tries < 1</code>
	 * @throws RejectedExecutionException if the downloader has been shutdown
	 */
	<T> Future<T> download(DownloadTask<T> task, DownloadTaskListener<T> listener, int tries);

}
