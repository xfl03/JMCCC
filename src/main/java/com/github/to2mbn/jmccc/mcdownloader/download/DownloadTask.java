package com.github.to2mbn.jmccc.mcdownloader.download;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

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

	private static class AppendedDownloadSession<R, S> implements DownloadSession<S> {

		ResultProcessor<R, S> processor;
		DownloadSession<R> proxied;

		AppendedDownloadSession(ResultProcessor<R, S> processor, DownloadSession<R> proxied) {
			this.processor = processor;
			this.proxied = proxied;
		}

		@Override
		public void receiveData(ByteBuffer data) throws IOException {
			proxied.receiveData(data);
		}

		@Override
		public S completed() throws IOException {
			return processor.process(proxied.completed());
		}

		@Override
		public void failed(Throwable e) throws IOException {
			proxied.failed(e);
		}

		@Override
		public void cancelled() throws IOException {
			proxied.cancelled();
		}

	}

	private static class AppendedDownloadTask<R, S> extends DownloadTask<S> {

		ResultProcessor<R, S> processor;
		DownloadTask<R> proxied;

		AppendedDownloadTask(ResultProcessor<R, S> processor, DownloadTask<R> proxied) {
			super(proxied.getURI());
			this.processor = processor;
			this.proxied = proxied;
		}

		@Override
		public DownloadSession<S> createSession() throws IOException {
			return new AppendedDownloadSession<>(processor, proxied.createSession());
		}

		@Override
		public DownloadSession<S> createSession(long length) throws IOException {
			return new AppendedDownloadSession<>(processor, proxied.createSession(length));
		}

	}

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
