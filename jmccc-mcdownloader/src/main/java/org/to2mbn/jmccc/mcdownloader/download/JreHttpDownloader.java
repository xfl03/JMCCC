package org.to2mbn.jmccc.mcdownloader.download;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.AsyncCallback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.AsyncCallbackGroup;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.AsyncFuture;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.Cancellable;

public class JreHttpDownloader implements DownloaderService {

	private static final int BUFFER_SIZE = 8192;

	private static final Logger LOGGER = Logger.getLogger(JreHttpDownloader.class.getCanonicalName());

	private class TaskHandler<T> implements Runnable, Cancellable {

		AsyncFuture<T> future;
		DownloadTask<T> task;
		DownloadCallback<T> downloadCallback;
		AsyncCallback<T> callback;
		int maxTries;
		volatile Future<?> taskfuture;
		volatile boolean cancelled;

		TaskHandler(DownloadTask<T> task, DownloadCallback<T> callback) {
			this.task = task;
			this.downloadCallback = callback;
			future = new AsyncFuture<>(this);
			this.callback = AsyncCallbackGroup.group(future, downloadCallback);
		}

		void start() {
			taskfuture = executor.submit(this);
			if (cancelled) {
				taskfuture.cancel(true);
			}
		}

		@Override
		public void run() {
			if (cancelled || Thread.interrupted()) {
				callback.cancelled();
				return;
			}

			try {
				T result = execute();
				callback.done(result);
			} catch (InterruptedException e) {
				callback.cancelled();
			} catch (Exception e) {
				callback.failed(e);
			}
		}

		T execute() throws IOException, InterruptedException, Exception {
			int currentTries = 0;
			for (;;) {
				try {
					return doDownload();
				} catch (IOException e) {
					currentTries++;
					if (currentTries < maxTries) {
						downloadCallback.retry(e, currentTries, maxTries);
					} else {
						throw e;
					}
				}
			}
		}

		T doDownload() throws IOException, InterruptedException, Exception {
			URLConnection connection = task.getURI().toURL().openConnection();
			connection.setReadTimeout(readTimeout);
			connection.setConnectTimeout(connectTimeout);
			connection.setRequestProperty("Accept", "*/*");
			connection.setRequestProperty("Connection", "keep-alive");
			if (connection instanceof HttpURLConnection) {
				((HttpURLConnection) connection).setRequestMethod("GET");
			}
			connection.connect();

			String contentLengthStr = connection.getHeaderField("Content-Length");
			long contentLength = -1;
			if (contentLengthStr != null) {
				try {
					contentLength = Long.parseLong(contentLengthStr);
					if (contentLength < 0) {
						LOGGER.warning("Invalid Content-Length: " + contentLengthStr + ", ignore");
						contentLength = -1;
					}
				} catch (NumberFormatException e) {
					LOGGER.warning("Invalid Content-Length: " + contentLengthStr + ", ignore: " + e);
				}
			}
			DownloadSession<T> session;
			if (contentLength == -1) {
				session = task.createSession(contentLength);
			} else {
				session = task.createSession();
			}

			try (InputStream in = connection.getInputStream()) {
				byte[] buf = new byte[BUFFER_SIZE];
				int read;
				while ((read = in.read(buf)) != -1) {
					if (Thread.interrupted()) {
						throw new InterruptedException();
					}
					session.receiveData(ByteBuffer.wrap(buf, 0, read));
				}
			} catch (InterruptedException e) {
				session.cancelled();
				throw e;
			} catch (Throwable e) {
				session.failed(e);
				throw e;
			}
			return session.completed();
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			if (taskfuture != null) {
				taskfuture.cancel(mayInterruptIfRunning);
			} else {
				cancelled = true;
			}
			return true;
		}

	}

	private ExecutorService executor;
	private int connectTimeout;
	private int readTimeout;

	private volatile boolean shudown = false;
	private ReadWriteLock shutdownLock = new ReentrantReadWriteLock();

	public JreHttpDownloader(int maxConns, int connectTimeout, int readTimeout, long poolThreadLivingTime) {
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		executor = new ThreadPoolExecutor(0, maxConns, poolThreadLivingTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}

	@Override
	public <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback) {
		return download(task, callback, 1);
	}

	@Override
	public <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback, int tries) {
		Objects.requireNonNull(task);
		if (tries < 1) {
			throw new IllegalArgumentException("tries < 1");
		}
		if (callback == null) {
			callback = new NullDownloadCallback<>();
		}

		Lock lock = shutdownLock.readLock();
		lock.lock();
		try {
			if (shudown) {
				throw new RejectedExecutionException("already shutdowm");
			}

			TaskHandler<T> handler = new TaskHandler<>(task, callback);
			handler.start();
			return handler.future;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void shutdown() {
		if (shudown) {
			return;
		}

		Lock lock = shutdownLock.writeLock();
		lock.lock();
		try {
			if (shudown) {
				return;
			}
			shudown = true;
		} finally {
			lock.unlock();
		}

		for (Runnable task : executor.shutdownNow()) {
			if (task instanceof Cancellable) {
				((Cancellable) task).cancel(true);
			}
		}
	}

	@Override
	public boolean isShutdown() {
		return shudown;
	}

}
