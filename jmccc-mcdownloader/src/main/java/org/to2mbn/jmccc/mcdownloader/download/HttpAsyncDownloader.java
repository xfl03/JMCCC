package org.to2mbn.jmccc.mcdownloader.download;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncByteConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.protocol.HttpContext;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.Callback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.CallbackGroup;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.AsyncFuture;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.Cancellable;

public class HttpAsyncDownloader implements DownloaderService {

	private static final Log LOGGER = LogFactory.getLog(HttpAsyncDownloader.class);

	private class TaskHandler<T> implements Runnable, Cancellable {

		class LifeCycleHandler implements Callback<T> {

			Callback<T> proxied;

			LifeCycleHandler(Callback<T> proxied) {
				this.proxied = proxied;
			}

			@Override
			public void failed(Throwable e) {
				if (session != null) {
					try {
						session.failed();
					} catch (Throwable e1) {
						if (e != e1) {
							e.addSuppressed(e1);
						}
					}
				}

				boolean retry = tryRetry(e);
				if (retry) {
					callback.retry(e, currentTries, maxTries);

					Lock lock = shutdownLock.readLock();
					lock.lock();
					try {
						if (!shutdown) {
							downloadFuture = null;
							session = null;
							start();
							return;
						}
					} finally {
						lock.unlock();
					}
				}
				proxied.failed(e);
			}

			@Override
			public void cancelled() {
				if (session != null) {
					try {
						session.failed();
					} catch (Throwable e) {
						proxied.failed(e);
						return;
					}
				}
				proxied.cancelled();
			}

			@Override
			public void done(T result) {
				proxied.done(result);
			}

		}

		class Inactiver implements Callback<T> {

			@Override
			public void done(T result) {
				inactive();
			}

			@Override
			public void failed(Throwable e) {
				inactive();
			}

			@Override
			public void cancelled() {
				inactive();
			}

			void inactive() {
				Lock lock = shutdownLock.readLock();
				lock.lock();
				try {
					activeTasks.remove(TaskHandler.this);
				} finally {
					lock.unlock();
				}

				if (shutdown & !shutdownComplete) {
					boolean doShutdown = false;
					Lock wlock = shutdownLock.writeLock();
					wlock.lock();
					try {
						if (!shutdownComplete) {
							shutdownComplete = true;
							doShutdown = true;
						}
					} finally {
						wlock.unlock();
					}

					if (doShutdown) {
						completeShutdown();
					}
				}
			}

		}

		DownloadTask<T> task;
		AsyncFuture<T> futuer;
		Callback<T> lifecycle;
		DownloadSession<T> session;
		DownloadCallback<T> callback;
		Future<T> downloadFuture;
		volatile boolean cancelled;
		volatile Throwable resultBuildingEx;
		int maxTries;
		int currentTries = 0;

		TaskHandler(DownloadTask<T> task, DownloadCallback<T> callback, int maxTries) {
			this.task = task;
			this.callback = callback;
			this.futuer = new AsyncFuture<>(this);
			this.lifecycle = new LifeCycleHandler(CallbackGroup.group(new Inactiver(), futuer, callback));
			this.maxTries = maxTries;
		}

		void start() {
			bootstrapPool.execute(this);
		}

		@Override
		public void run() {
			try {
				if (shutdown || cancelled) {
					lifecycle.cancelled();
					return;
				}

				downloadFuture = httpClient.execute(HttpAsyncMethods.createGet(task.getURI()), new AsyncByteConsumer<T>() {

					long contextLength = -1;
					long received = 0;

					@Override
					protected void onByteReceived(ByteBuffer buf, IOControl ioctrl) throws IOException {
						if (session == null) {
							session = task.createSession(8192);
						}
						received += buf.remaining();
						session.receiveData(buf);
						callback.updateProgress(received, contextLength);
					}

					@Override
					protected void onResponseReceived(HttpResponse response) throws HttpException, IOException {
						if (response.getStatusLine() != null) {
							int statusCode = response.getStatusLine().getStatusCode();
							if (statusCode < 200 || statusCode > 299) {
								// non-2xx response code
								throw new IOException("Illegal http response code: " + statusCode);
							}
						}
						if (session == null) {
							HttpEntity httpEntity = response.getEntity();
							if (httpEntity != null) {
								long contextLength = httpEntity.getContentLength();
								if (contextLength >= 0) {
									this.contextLength = contextLength;
								}
							}
							session = task.createSession(contextLength > 0 ? contextLength : 8192);
						}
					}

					@Override
					protected T buildResult(HttpContext context) throws Exception {
						T result = null;
						try {
							result = session.completed();
							resultBuildingEx = null;
						} catch (Throwable e) {
							resultBuildingEx = e;
						}
						return result;
					}

				}, new FutureCallback<T>() {

					@Override
					public void completed(T result) {
						if (resultBuildingEx != null) {
							lifecycle.failed(resultBuildingEx);
						} else {
							lifecycle.done(result);
						}
					}

					@Override
					public void failed(Exception e) {
						lifecycle.failed(e);
					}

					@Override
					public void cancelled() {
						lifecycle.cancelled();
					}
				});

				if (cancelled) {
					downloadFuture.cancel(true);
				}
			} catch (Throwable e) {
				lifecycle.failed(e);
			}
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			cancelled = true;
			if (downloadFuture != null) {
				downloadFuture.cancel(true);
			}
			return true;
		}

		boolean tryRetry(Throwable ex) {
			if (ex instanceof IOException || ex instanceof TimeoutException) {
				currentTries++;
				return currentTries < maxTries;
			} else {
				return false;
			}
		}

	}

	private CloseableHttpAsyncClient httpClient;
	private Executor bootstrapPool;
	private volatile boolean shutdown = false;
	private volatile boolean shutdownComplete = false;
	private Set<TaskHandler<?>> activeTasks = Collections.newSetFromMap(new ConcurrentHashMap<TaskHandler<?>, Boolean>());
	// lock for shutdown, shutdownComplete, activeTasks
	private ReadWriteLock shutdownLock = new ReentrantReadWriteLock();

	public HttpAsyncDownloader(HttpAsyncClientBuilder builder, Executor bootstrapPool) {
		this.bootstrapPool = bootstrapPool;
		httpClient = builder.build();
		httpClient.start();
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
		callback = nonNullDownloadListener(callback);
		Lock lock = shutdownLock.readLock();
		lock.lock();
		try {
			if (shutdown) {
				throw new RejectedExecutionException("already shutdown");
			}
			TaskHandler<T> handler = new TaskHandler<>(task, callback, tries);
			activeTasks.add(handler);
			handler.start();
			return handler.futuer;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void shutdown() {
		if (!shutdown) {
			Lock wlock = shutdownLock.writeLock();
			wlock.lock();
			try {
				if (shutdown) {
					return;
				}
				shutdown = true;
			} finally {
				wlock.unlock();
			}

			bootstrapPool = null;

			if (activeTasks.isEmpty()) {
				// cleanup in current thread
				shutdownComplete = true;
				completeShutdown();
			} else {
				// cancel all the tasks and let the latest task cleanup
				for (TaskHandler<?> task : activeTasks) {
					task.cancel(true);
				}
			}
		}
	}

	@Override
	public boolean isShutdown() {
		return shutdown;
	}

	private <T> DownloadCallback<T> nonNullDownloadListener(DownloadCallback<T> o) {
		return o == null ? new NullDownloadCallback<T>() : o;
	}

	private void completeShutdown() {
		try {
			httpClient.close();
		} catch (IOException e) {
			LOGGER.error("an exception occurred during shutdown http client", e);
		}
		httpClient = null;
	}

}
