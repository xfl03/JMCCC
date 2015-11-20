package com.github.to2mbn.jmccc.mcdownloader.download.group;

import java.util.concurrent.Future;

public interface MultipleDownloader {

	/**
	 * Submits a multiple download task asynchronously.
	 * 
	 * @param task download task
	 * @param callback download callback
	 * @param tries the max number of tries for each sub download task
	 * @return future representing pending completion of the download
	 */
	<T> Future<T> download(MultipleDownloadTask<T> task, MultipleDownloadCallback<T> callback, int tries);

}
