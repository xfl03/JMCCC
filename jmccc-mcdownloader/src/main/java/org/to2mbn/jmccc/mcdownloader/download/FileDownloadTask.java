package org.to2mbn.jmccc.mcdownloader.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Objects;
import org.to2mbn.jmccc.util.FileUtils;

/**
 * Describes a file download task.
 * 
 * @author yushijinhun
 */
public class FileDownloadTask extends DownloadTask<Void> {

	private File target;

	/**
	 * Constructs a FileDownloadTask.
	 * 
	 * @param uri the uri of the resource to download
	 * @param target the folder to save the file
	 * @throws NullPointerException if <code>uri==null || target==null</code>
	 * @throws IllegalArgumentException if <code>uri</code> is not in a valid
	 *             URI format
	 */
	public FileDownloadTask(String uri, File target) {
		super(uri);
		Objects.requireNonNull(target);
		this.target = target;
	}

	/**
	 * Constructs a FileDownloadTask.
	 * 
	 * @param uri the uri of the resource to download
	 * @param target the folder to save the file
	 * @throws NullPointerException if <code>uri==null || target==null</code>
	 */
	public FileDownloadTask(URI uri, File target) {
		super(uri);
		Objects.requireNonNull(target);
		this.target = target;
	}

	/**
	 * Gets the folder to save the file downloaded.
	 * 
	 * @return the folder to save the file downloaded
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
	public DownloadSession<Void> createSession() throws IOException {
		final File partFile = new File(target.getParentFile(), target.getName() + ".part");

		FileUtils.prepareWrite(partFile);
		
		final FileOutputStream out = new FileOutputStream(partFile);
		final FileChannel channel = out.getChannel();

		return new DownloadSession<Void>() {

			@Override
			public void receiveData(ByteBuffer data) throws IOException {
				channel.write(data);
			}

			@Override
			public void failed() throws IOException {
				close();
				partFile.delete();
			}

			@Override
			public Void completed() throws IOException {
				close();
				FileUtils.prepareWrite(target);
				if (target.exists()) {
					target.delete();
				}
				partFile.renameTo(target);
				return null;
			}

			private void close() throws IOException {
				out.close();
			}
		};
	}

}
