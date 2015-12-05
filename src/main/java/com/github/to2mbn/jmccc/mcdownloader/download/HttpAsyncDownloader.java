package com.github.to2mbn.jmccc.mcdownloader.download;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
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
import com.github.to2mbn.jmccc.mcdownloader.download.concurrent.AsyncCallback;
import com.github.to2mbn.jmccc.mcdownloader.download.concurrent.AsyncCallbackGroup;
import com.github.to2mbn.jmccc.mcdownloader.download.concurrent.AsyncFuture;
import com.github.to2mbn.jmccc.mcdownloader.download.concurrent.Cancellable;

public class HttpAsyncDownloader implements DownloaderService {

	private static final Log LOGGER = LogFactory.getLog(HttpAsyncDownloader.class);

	private static class NullDownloadCallback<T> implements DownloadCallback<T> {

		@Override
		public void done(T result) {
		}

		@Override
		public void failed(Throwable e) {
		}

		@Override
		public void cancelled() {
		}

		@Override
		public void updateProgress(long done, long total) {
		}

		@Override
		public void retry(Throwable e, int current, int max) {
		}

	}

	private static interface RetryHandler {

		boolean doRetry(Throwable e);

	}

	private static class RetryHandlerImpl implements RetryHandler {

		DownloadCallback<?> proxied;
		int max;
		int current = 1;

		RetryHandlerImpl(DownloadCallback<?> proxied, int max) {
			this.proxied = proxied;
			this.max = max;
		}

		@Override
		public boolean doRetry(Throwable e) {
			if (e instanceof IOException && current < max) {
				proxied.retry(e, current++, max);
				return true;
			}
			return false;
		}

	}

	private class TaskHandler<T> implements Runnable, Cancellable {

		class LifeCycleHandler<R> {

			AsyncCallback<R> proxied;

			LifeCycleHandler(AsyncCallback<R> proxied) {
				this.proxied = proxied;
			}

			void failed(Throwable e) {
				if (session != null) {
					try {
						session.failed(e);
					} catch (Throwable e1) {
						if (e != e1) {
							e.addSuppressed(e1);
						}
					}
				}
				if (retryHandler != null) {
					Lock lock = shutdownLock.readLock();
					lock.lock();
					try {
						if (!shutdown && retryHandler.doRetry(e)) {
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

			void cancelled() {
				if (session != null) {
					try {
						session.cancelled();
					} catch (Throwable e) {
						proxied.failed(e);
						return;
					}
				}
				proxied.cancelled();
			}

			void done(R result) {
				proxied.done(result);
			}
		}

		class Inactiver implements AsyncCallback<T> {

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
		LifeCycleHandler<T> lifecycle;
		DownloadSession<T> session;
		DownloadCallback<T> callback;
		Future<T> downloadFuture;
		RetryHandler retryHandler;
		volatile boolean cancelled;

		TaskHandler(DownloadTask<T> task, DownloadCallback<T> callback, RetryHandler retryHandler) {
			this.task = task;
			this.callback = callback;
			this.futuer = new AsyncFuture<>(this);
			this.lifecycle = new LifeCycleHandler<>(AsyncCallbackGroup.group(new Inactiver(), futuer, callback));
			this.retryHandler = retryHandler;
		}

		void start() {
			bootstrapPool.execute(this);
		}

		@Override
		public void run() {
			try {
				Lock lock = shutdownLock.readLock();
				lock.lock();
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
								if (statusCode < 200 || statusCode > 299) { // not 2xx
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
							return session.completed();
						}

					}, new FutureCallback<T>() {

						@Override
						public void completed(T result) {
							lifecycle.done(result);
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
				} finally {
					lock.unlock();
				}

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
		Objects.requireNonNull(task);
		return download0(task, nonNullDownloadListener(callback), null);
	}

	@Override
	public <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback, int tries) {
		Objects.requireNonNull(task);
		if (tries < 1) {
			throw new IllegalArgumentException("tries < 1");
		}
		callback = nonNullDownloadListener(callback);
		return download0(task, callback, new RetryHandlerImpl(callback, tries));
	}

	@Override
	public void shutdown() {
		if (!shutdown) {
			Lock lock = shutdownLock.writeLock();
			lock.lock();
			try {
				shutdown = true;
			} finally {
				lock.unlock();
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

	private <T> Future<T> download0(DownloadTask<T> task, DownloadCallback<T> callback, RetryHandler retryHandler) {
		Lock lock = shutdownLock.readLock();
		lock.lock();
		try {
			if (shutdown) {
				throw new RejectedExecutionException("already shutdown");
			}
			TaskHandler<T> handler = new TaskHandler<>(task, callback, retryHandler);
			activeTasks.add(handler);
			handler.start();
			return handler.futuer;
		} finally {
			lock.unlock();
		}
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
