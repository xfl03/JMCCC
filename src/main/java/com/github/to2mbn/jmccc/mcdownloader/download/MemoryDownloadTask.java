package com.github.to2mbn.jmccc.mcdownloader.download;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
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
	public DownloadSession createSession() throws IOException {
		return new DownloadSession() {

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			WritableByteChannel channel = Channels.newChannel(out);

			@Override
			public void receiveData(ByteBuffer data) throws IOException {
				channel.write(data);
			}

			@Override
			public void failed(Throwable e) throws IOException {
				close();
			}

			@Override
			public void completed() throws IOException {
				callback.downloaded(out.toByteArray());
				close();
			}

			@Override
			public void cancelled() throws IOException {
				close();
			}

			private void close() {
				channel = null;
				out = null;
			}
		};
	}

}
