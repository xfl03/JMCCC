package com.github.to2mbn.jmccc.mcdownloader.download;

public interface DownloadTaskListener {

	/**
	 * Calls when the download operation started.
	 */
	void started();

	/**
	 * Calls when the download operation completed successfully.
	 */
	void completed();

	/**
	 * Calls when the download operation failed.
	 * 
	 * @param e the thrown exception
	 */
	void failed(Throwable e);

	/**
	 * Calls when the download operation has been cancelled.
	 */
	void cancelled();

	/**
	 * Calls when the progress of the download operation has been updated.
	 * 
	 * @param done the downloaded bytes
	 * @param total the total bytes
	 */
	void updateProgress(long done, long total);

}
