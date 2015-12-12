package com.github.to2mbn.jmccc.mcdownloader.download.multiple;

import com.github.to2mbn.jmccc.mcdownloader.download.DownloadCallback;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import com.github.to2mbn.jmccc.mcdownloader.download.concurrent.AsyncCallback;

public interface MultipleDownloadCallback<T> extends AsyncCallback<T> {

	/**
	 * Calls when a new sub download task starts.
	 * 
	 * @param task the sub download task
	 * @param <R> the type of the sub download task
	 * @return the callback to register to the download task, can be null
	 */
	<R> DownloadCallback<R> taskStart(DownloadTask<R> task);

}
