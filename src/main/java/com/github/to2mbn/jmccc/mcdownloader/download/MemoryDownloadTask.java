package com.github.to2mbn.jmccc.mcdownloader.download;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Objects;

/**
 * A download-to-memory task.
 * 
 * @author yushijinhun
 */
public class MemoryDownloadTask extends DownloadTask {

	private MemoryDownloadCallback callback;

	/**
	 * Creates a MemoryDownloadTask.
	 * 
	 * @param url the url of the resource to download
	 * @param callback the download callback
	 * @throws NullPointerException if <code>url==null||callback==null</code>
	 */
	public MemoryDownloadTask(URL url, MemoryDownloadCallback callback) {
		super(url);
		Objects.requireNonNull(callback);
		this.callback = callback;
	}

	@Override
	public OutputStream openStream() throws IOException {
		return new ByteArrayOutputStream() {

			@Override
			public void close() throws IOException {
				callback.downloaded(toByteArray());
			}

		};
	}

}
