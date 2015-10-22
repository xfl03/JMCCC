package com.github.to2mbn.jmccc.mcdownloader.download;

import java.io.IOException;
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
	 * Calls when the download task begins.
	 * 
	 * @return a new download session
	 * @throws IOException if an I/O error occurs
	 */
	abstract public DownloadSession createSession() throws IOException;

}
