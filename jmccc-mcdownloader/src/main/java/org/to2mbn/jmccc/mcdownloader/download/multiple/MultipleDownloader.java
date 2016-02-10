package org.to2mbn.jmccc.mcdownloader.download.multiple;

import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.Shutdownable;

public interface MultipleDownloader extends Shutdownable {

	/**
	 * Submits a multiple download task asynchronously.
	 * 
	 * @param task download task
	 * @param callback download callback
	 * @param tries the max number of tries for each sub download task
	 * @param <T> the type of the MultipleDownloadTask
	 * @return future representing pending completion of the download
	 * @throws NullPointerException if <code>task==null</code>
	 * @throws IllegalArgumentException if <code>tries &lt; 1</code>
	 * @throws RejectedExecutionException if the downloader has been shutdown
	 */
	<T> Future<T> download(MultipleDownloadTask<T> task, MultipleDownloadCallback<T> callback, int tries);

}
