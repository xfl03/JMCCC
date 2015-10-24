package com.github.to2mbn.jmccc.mcdownloader.download;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

/**
 * Describes a download task.
 * <p>
 * A download task has a uri of the resource to download, and a save location, such as file, memory. The save location
 * is handled by subclasses.
 * 
 * @param <T> the type of result
 * @author yushijinhun
 */
abstract public class DownloadTask<T> {

	private URI uri;

	/**
	 * Creates a DownloadTask.
	 * 
	 * @param uri the uri of the resource to download
	 * @throws NullPointerException if <code>uri==null</code>
	 */
	public DownloadTask(URI uri) {
		this.uri = uri;
	}

	/**
	 * Gets the uri of the resource to download.
	 * 
	 * @return the uri of the resource to download
	 */
	public URI getURI() {
		return uri;
	}

	@Override
	public int hashCode() {
		return Objects.hash(uri);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof DownloadTask) {
			DownloadTask<?> another = (DownloadTask<?>) obj;
			return uri.equals(another.uri);
		}
		return false;
	}

	/**
	 * Calls when the download task begins.
	 * 
	 * @return a new download session
	 * @throws IOException if an I/O error occurs
	 */
	abstract public DownloadSession<T> createSession() throws IOException;

	/**
	 * Calls when the download task begins.
	 * 
	 * @param length the possible length of data
	 * @return a new download session
	 * @throws IOException if an I/O error occurs
	 */
	public DownloadSession<T> createSession(long length) throws IOException {
		return createSession();
	}

}
