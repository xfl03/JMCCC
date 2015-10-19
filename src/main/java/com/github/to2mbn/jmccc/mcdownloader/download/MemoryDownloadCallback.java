package com.github.to2mbn.jmccc.mcdownloader.download;

public interface MemoryDownloadCallback {

	/**
	 * Calls when all the data has been downloaded.
	 * 
	 * @param data the downloaded data
	 */
	void downloaded(byte[] data);

}
