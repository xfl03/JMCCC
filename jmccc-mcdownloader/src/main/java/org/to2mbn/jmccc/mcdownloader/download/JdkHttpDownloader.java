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
import java.util.concurrent.Callable;
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
import org.to2mbn.jmccc.mcdownloader.download.concurrent.Callback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.CallbackFutureTask;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.Callbacks;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.EmptyCallback;

public class JdkHttpDownloader implements DownloaderService {

	private static final int BUFFER_SIZE = 8192;

	private static final Logger LOGGER = Logger.getLogger(JdkHttpDownloader.class.getCanonicalName());

	private class CallableDownloadTask<T> implements Callable<T> {

		private final DownloadTask<T> task;
		private final DownloadCallback<T> callback;
		private final int maxTries;

		public CallableDownloadTask(DownloadTask<T> task, DownloadCallback<T> callback, int maxTries) {
			Objects.requireNonNull(task);
			Objects.requireNonNull(callback);
			if (maxTries < 1)
				throw new IllegalArgumentException(String.valueOf(maxTries));

			this.task = task;
			this.callback = callback;
			this.maxTries = maxTries;
		}

		@Override
		public T call() throws Exception {
			int currentTries = 0;
			for (;;) {
				try {
					return download();
				} catch (IOException e) {
					checkInterrupted();
					currentTries++;
					if (currentTries < maxTries) {
						callback.retry(e, currentTries, maxTries);
					} else {
						throw e;
					}
				}
			}
		}

		private T download() throws IOException, InterruptedException, Exception {
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

				DownloadSession<T> session = (contentLength == -1)
						? task.createSession()
						: task.createSession(contentLength);

				long downloaded = 0;

				try (InputStream in = connection.getInputStream()) {
					byte[] buf = new byte[BUFFER_SIZE];
					int read;
					while ((read = in.read(buf)) != -1) {
						checkInterrupted();
						downloaded += read;
						session.receiveData(ByteBuffer.wrap(buf, 0, read));
						callback.updateProgress(downloaded, contentLength);
					}
				} catch (Throwable e) {
					session.failed();
					throw e;
				}
				return session.completed();
			} finally {
				if (connection instanceof HttpURLConnection) {
					((HttpURLConnection) connection).disconnect();
				}
			}
		}

		private void checkInterrupted() throws InterruptedException {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
		}

	}

	private class TaskInactiver implements Runnable {

		private final Future<?> future;

		public TaskInactiver(Future<?> future) {
			Objects.requireNonNull(future);
			this.future = future;
		}

		@Override
		public void run() {
			tasks.remove(future);
		}

	}

	private ExecutorService executor;

	private int connectTimeout;
	private int readTimeout;
	private Proxy proxy;

	private volatile boolean shutdown;
	private final ReadWriteLock rwlock = new ReentrantReadWriteLock();
	private final Set<Future<?>> tasks = Collections.newSetFromMap(new ConcurrentHashMap<Future<?>, Boolean>());

	public JdkHttpDownloader(int maxConns, int connectTimeout, int readTimeout, long poolThreadLivingTime, Proxy proxy) {
		Objects.requireNonNull(proxy);

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
	public <T> Future<T> download(DownloadTask<T> downloadTask, DownloadCallback<T> callback, int tries) {
		/*
		 * # Submit task
		 * ++++ read lock
		 * 	1. Has the shutdown flag been set? ................................................... read shutdown
		 * 		Yes - Reject execution.
		 * 		No - Go on.
		 * 
		 * 	2. Create a task handler, store it in tasks. ......................................... write tasks
		 * 
		 * 	3. Start the task handler. ........................................................... read executor
		 * ---- read unlock
		 */

		Objects.requireNonNull(downloadTask);
		if (tries < 1)
			throw new IllegalArgumentException("tries < 1");

		CallbackFutureTask<T> task = new CallbackFutureTask<>(new CallableDownloadTask<>(
				downloadTask,
				callback == null ? new EmptyCallback<T>() : callback,
				tries));

		Callback<T> statusCallback = Callbacks.whatever(new TaskInactiver(task));
		if (callback != null) {
			statusCallback = Callbacks.group(statusCallback, callback);
		}
		task.setCallback(callback);

		Lock lock = rwlock.readLock();
		lock.lock();
		try {
			if (shutdown)
				throw new RejectedExecutionException("The downloader has been shutdown.");

			tasks.add(task);
			executor.execute(task);
		} finally {
			lock.unlock();
		}

		return task;
	}

	@Override
	public void shutdown() {
		/*
		 * # Shutdown
		 * ++++ write lock
		 * 	1. Has the shutdown flag been set? ................................................... read shutdown
		 * 		Yes - Do nothing.
		 * 		No - Go on.
		 * 	
		 * 	2. Set the shutdown flag. ............................................................ write shutdown
		 * 
		 * 	3. Cancel all the tasks. ............................................................. read tasks
		 * 
		 * 	4. Shutdown the executor. ............................................................ write executor
		 * ---- write unlock
		 */

		Lock lock = rwlock.writeLock();
		lock.lock();
		try {
			if (shutdown) {
				return;
			}

			shutdown = true;
		} finally {
			lock.unlock();
		}

		for (Future<?> task : tasks)
			task.cancel(true);

		executor.shutdownNow();
		executor = null;
	}

	@Override
	public boolean isShutdown() {
		return shutdown;
	}

}
