package com.github.to2mbn.jmccc.mcdownloader.download;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Objects;

/**
 * Describes a download task.
 * <p>
 * A download task has a url of the resource to download, and a save location, such as file, memory. The save location
 * is handled by subclasses.
 * 
 * @author yushijinhun
 */
abstract public class DownloadTask {

	private URL url;

	/**
	 * Creates a DownloadTask.
	 * 
	 * @param url the url of the resource to download
	 * @throws NullPointerException if <code>url==null</code>
	 */
	public DownloadTask(URL url) {
		this.url = url;
	}

	/**
	 * Gets the url of the resource to download.
	 * 
	 * @return the url of the resource to download
	 */
	public URL getURL() {
		return url;
	}

	@Override
	public int hashCode() {
		return Objects.hash(url);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof DownloadTask) {
			DownloadTask another = (DownloadTask) obj;
			return url.equals(another.url);
		}
		return false;
	}

	/**
	 * Opens a output stream to the target.
	 * <p>
	 * This method will be called when the download starts. The downloaded data will be written to the output stream.
	 * The output stream can be a file, a buffer or a socket. When the download finished, failed, or cancelled. The
	 * <code>close()</code> of the stream will be called.
	 * 
	 * @return a output stream to the target
	 * @throws IOException if an i/o error occurs
	 */
	abstract public OutputStream openStream() throws IOException;

}
