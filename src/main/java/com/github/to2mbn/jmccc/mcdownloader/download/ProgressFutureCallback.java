package com.github.to2mbn.jmccc.mcdownloader.download;

import org.apache.http.concurrent.FutureCallback;

public interface ProgressFutureCallback<T> extends FutureCallback<T> {

	/**
	 * Calls when the processing progress updates.
	 * 
	 * @param done the finished quantity of the task
	 * @param total the total quantity of the task
	 */
	void progressUpdate(long done, long total);

}
