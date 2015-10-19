package com.github.to2mbn.jmccc.mcdownloader.download;


public interface DownloadGroupFutureCallback<T> extends ProgressFutureCallback<T> {

	/**
	 * Calls when a sub download task starts.
	 * <p>
	 * If the returned listener is not null, it will be registered on the download task.
	 * 
	 * @param task the sub download task
	 * @return the listener to register
	 */
	ProgressFutureCallback<?> taskStart(DownloadTask task);
	
}
