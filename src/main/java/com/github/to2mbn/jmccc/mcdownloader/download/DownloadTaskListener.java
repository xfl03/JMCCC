package com.github.to2mbn.jmccc.mcdownloader.download;

import com.github.to2mbn.jmccc.mcdownloader.download.concurrent.AsyncCallback;

public interface DownloadTaskListener<T> extends AsyncCallback<T> {

	/**
	 * Calls when the progress of the download operation has been updated.
	 * 
	 * @param done the downloaded bytes
	 * @param total the total bytes, -1 if unknown
	 */
	void updateProgress(long done, long total);

}
