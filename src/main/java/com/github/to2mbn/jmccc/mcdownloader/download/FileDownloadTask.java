package com.github.to2mbn.jmccc.mcdownloader.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Objects;

/**
 * Describes a download-to-file task.
 * 
 * @author yushijinhun
 */
public class FileDownloadTask extends DownloadTask {

	private File target;

	/**
	 * Creates a FileDownloadTask.
	 * 
	 * @param url the url of the resource to download
	 * @param target where to save the file
	 * @throws NullPointerException if <code>url==null||target==null</code>
	 */
	public FileDownloadTask(URL url, File target) {
		super(url);
		Objects.requireNonNull(target);
		this.target = target;
	}

	/**
	 * Gets the save location.
	 * 
	 * @return the save location
	 */
	public File getTarget() {
		return target;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), target);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (super.equals(obj)) {
			FileDownloadTask another = (FileDownloadTask) obj;
			return target.equals(another.target);
		}
		return false;
	}

	@Override
	public DownloadSession createSession() throws IOException {
		final FileOutputStream out = new FileOutputStream(target);
		final FileChannel channel = out.getChannel();

		return new DownloadSession() {

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
				close();
			}

			@Override
			public void cancelled() throws IOException {
				close();
			}

			private void close() throws IOException {
				out.close();
			}
		};
	}

}
