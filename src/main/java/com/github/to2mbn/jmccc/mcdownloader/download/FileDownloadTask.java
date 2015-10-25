package com.github.to2mbn.jmccc.mcdownloader.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Objects;

/**
 * Describes a download-to-file task.
 * 
 * @author yushijinhun
 */
public class FileDownloadTask extends DownloadTask<Object> {

	private File target;

	/**
	 * Creates a FileDownloadTask.
	 * 
	 * @param uri the uri of the resource to download
	 * @param target where to save the file
	 * @throws NullPointerException if <code>uri==null||target==null</code>
	 */
	public FileDownloadTask(URI uri, File target) {
		super(uri);
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
	public DownloadSession<Object> createSession() throws IOException {
		// creates the parent dir
		File parent = target.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}

		final FileOutputStream out = new FileOutputStream(target);
		final FileChannel channel = out.getChannel();

		return new DownloadSession<Object>() {

			@Override
			public void receiveData(ByteBuffer data) throws IOException {
				channel.write(data);
			}

			@Override
			public void failed(Throwable e) throws IOException {
				close();
			}

			@Override
			public Object completed() throws IOException {
				close();
				return null;
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
