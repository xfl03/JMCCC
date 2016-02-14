package org.to2mbn.jmccc.mcdownloader.download;

import java.io.IOException;
import java.net.URI;

/**
 * Describes a download task.
 * <p>
 * A download task has the uri of resource to download, and the location to save, such as file, memory. The save location
 * is handled by subclasses.
 * 
 * @param <T> the type of result
 * @author yushijinhun
 */
abstract public class DownloadTask<T> {

	private URI uri;

	/**
	 * Constructor of DownloadTask.
	 * 
	 * @param uri the uri of resource to download
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

	public <R> DownloadTask<R> andThen(ResultProcessor<T, R> processor) {
		return new AppendedDownloadTask<>(processor, this);
	}

}
