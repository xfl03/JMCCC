package org.to2mbn.jmccc.mcdownloader.download.combine;

import org.to2mbn.jmccc.mcdownloader.download.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.Callback;

public interface CombinedDownloadCallback<T> extends Callback<T> {

	/**
	 * Calls when a new sub download task starts.
	 * 
	 * @param task the sub download task
	 * @param <R> the type of the sub download task
	 * @return the callback to register to the download task, can be null
	 */
	<R> DownloadCallback<R> taskStart(DownloadTask<R> task);

}
