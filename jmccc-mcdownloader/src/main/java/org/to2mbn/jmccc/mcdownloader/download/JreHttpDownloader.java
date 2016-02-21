package org.to2mbn.jmccc.mcdownloader.download;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

		class LifecycleHandler implements AsyncCallback<T> {

			AsyncCallback<T> proxied;
			volatile boolean terminated;

			LifecycleHandler(AsyncCallback<T> proxied) {
				this.proxied = proxied;
			}

			@Override
			public void done(T result) {
				if (terminate()) {
					proxied.done(result);
				}
			}

			@Override
			public void failed(Throwable e) {
				if (terminate()) {
					proxied.failed(e);
				}
			}

			@Override
			public void cancelled() {
				if (terminate()) {
					proxied.cancelled();
				}
			}

			boolean terminate() {
				synchronized (this) {
					if (!terminated) {
						terminated = true;
						tasks.remove(TaskHandler.this);
						return true;
					}
				}
				return false;
			}

		}

		AsyncFuture<T> future;
		DownloadTask<T> task;
		DownloadCallback<T> downloadCallback;
		AsyncCallback<T> callback;
		int maxTries;
		volatile Future<?> taskfuture;
		volatile boolean cancellInFuture;

		TaskHandler(DownloadTask<T> task, DownloadCallback<T> callback, int maxTries) {
			this.task = task;
			this.downloadCallback = callback;
			this.maxTries = maxTries;
			future = new AsyncFuture<>(this);
			this.callback = new LifecycleHandler(AsyncCallbackGroup.group(future, downloadCallback));
		}

		void start() {
			taskfuture = executor.submit(this);
			if (cancellInFuture) {
				taskfuture.cancel(true);
			}
		}

		@Override
		public void run() {
			if (cancellInFuture || Thread.interrupted()) {
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
					checkInterrupted();
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
			URLConnection connection = task.getURI().toURL().openConnection(proxy);
			connection.setReadTimeout(readTimeout);
			connection.setConnectTimeout(connectTimeout);
			connection.setRequestProperty("Accept", "*/*");
			connection.setRequestProperty("Connection", "keep-alive");
			if (connection instanceof HttpURLConnection) {
				((HttpURLConnection) connection).setRequestMethod("GET");
			}
			connection.connect();

			try {
				if (connection instanceof HttpURLConnection) {
					int responseCode = ((HttpURLConnection) connection).getResponseCode();
					if (responseCode < 200 || responseCode > 299) {
						// non-2xx response code
						throw new IOException("Illegal http response code: " + responseCode);
					}
				}

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

				checkInterrupted();

				DownloadSession<T> session;
				if (contentLength == -1) {
					session = task.createSession(contentLength);
				} else {
					session = task.createSession();
				}

				long downloaded = 0;

				try (InputStream in = connection.getInputStream()) {
					byte[] buf = new byte[BUFFER_SIZE];
					int read;
					while ((read = in.read(buf)) != -1) {
						checkInterrupted();
						downloaded += read;
						session.receiveData(ByteBuffer.wrap(buf, 0, read));
						downloadCallback.updateProgress(downloaded, contentLength);
					}
				} catch (InterruptedException e) {
					session.cancelled();
					throw e;
				} catch (Throwable e) {
					session.failed(e);
					throw e;
				}
				return session.completed();
			} finally {
				if (connection instanceof HttpURLConnection) {
					((HttpURLConnection) connection).disconnect();
				}
			}
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			if (taskfuture == null || !taskfuture.cancel(mayInterruptIfRunning)) {
				cancellInFuture = true;
				callback.cancelled();
			}
			return true;
		}

		private void checkInterrupted() throws InterruptedException {
			if (cancellInFuture || Thread.interrupted()) {
				throw new InterruptedException();
			}
		}

	}

	private ExecutorService executor;
	private int connectTimeout;
	private int readTimeout;
	private Proxy proxy;

	private volatile boolean shudown = false;
	private ReadWriteLock shutdownLock = new ReentrantReadWriteLock();
	private Set<TaskHandler<?>> tasks = Collections.newSetFromMap(new ConcurrentHashMap<TaskHandler<?>, Boolean>());

	public JreHttpDownloader(int maxConns, int connectTimeout, int readTimeout, long poolThreadLivingTime, Proxy proxy) {
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.proxy = proxy;
		executor = new ThreadPoolExecutor(maxConns, maxConns, poolThreadLivingTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
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

			TaskHandler<T> handler = new TaskHandler<>(task, callback, tries);
			tasks.add(handler);
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

		executor.shutdownNow();
		for (TaskHandler<?> task : tasks) {
			task.cancel(true);
		}
	}

	@Override
	public boolean isShutdown() {
		return shudown;
	}

}
