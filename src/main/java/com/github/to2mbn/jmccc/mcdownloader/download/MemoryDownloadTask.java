package com.github.to2mbn.jmccc.mcdownloader.download;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

/**
 * A download-to-memory task.
 * 
 * @author yushijinhun
 */
public class MemoryDownloadTask extends DownloadTask<byte[]> {

	/**
	 * Creates a MemoryDownloadTask.
	 * 
	 * @param uri the uri of the resource to download
	 * @throws NullPointerException if <code>uri==null</code>
	 */
	public MemoryDownloadTask(URI uri) {
		super(uri);
	}

	@Override
	public DownloadSession<byte[]> createSession(final long length) throws IOException {
		return new DownloadSession<byte[]>() {

			ByteArrayOutputStream out = new ByteArrayOutputStream((int) length);
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
			public byte[] completed() throws IOException {
				byte[] data = out.toByteArray();
				close();
				return data;
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

	@Override
	public DownloadSession<byte[]> createSession() throws IOException {
		return createSession(8192);
	}

}
