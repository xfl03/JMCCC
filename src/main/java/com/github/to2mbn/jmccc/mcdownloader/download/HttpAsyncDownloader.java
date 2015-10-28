package com.github.to2mbn.jmccc.mcdownloader.download;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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

public class HttpAsyncDownloader implements Downloader {

	private static class NullDownloadTaskListener<T> implements DownloadTaskListener<T> {

		@Override
		public void done(T result) {
		}

		@Override
		public void failed(Throwable e) {
			synchronized (System.err) {
				System.err.println("Uncaught async download exception:");
				e.printStackTrace();
			}
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

	private static final int DEFAULT_MAX_CONNECTIONS = 50;
	private static final int DEFAULT_MAX_CONNECTIONS_PRE_ROUTER = 10;

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
				if (retryHandler != null && retryHandler.doRetry(e)) {
					downloadFuture = null;
					session = null;
					start();
				} else {
					proxied.failed(e);
				}
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

		DownloadTask<T> task;
		AsyncFuture<T> futuer;
		LifeCycleHandler<T> lifecycle;
		DownloadSession<T> session;
		DownloadTaskListener<T> listener;
		Future<T> downloadFuture;
		RetryHandler retryHandler;
		volatile boolean cancelled;
		volatile boolean mayInterruptIfRunning;

		TaskHandler(DownloadTask<T> task, DownloadTaskListener<T> listener, RetryHandler retryHandler) {
			this.task = task;
			this.listener = listener;
			this.futuer = new AsyncFuture<>(this);
			this.lifecycle = new LifeCycleHandler<>(AsyncCallbackGroup.group(futuer, listener));
			this.retryHandler = retryHandler;
		}

		void start() {
			bootstrapPool.submit(this);
		}

		@Override
		public void run() {
			try {
				Lock lock = shutdownLock.readLock();
				lock.lock();
				try {
					if (shutdown) {
						lifecycle.cancelled();
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
							listener.updateProgress(received, contextLength);
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

					if (cancelled) {
						downloadFuture.cancel(mayInterruptIfRunning);
					}
				} finally {
					lock.unlock();
				}
			} catch (Throwable e) {
				lifecycle.failed(e);
			}
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			cancelled = true;
			this.mayInterruptIfRunning = mayInterruptIfRunning;
			if (downloadFuture != null) {
				downloadFuture.cancel(mayInterruptIfRunning);
			}
			return true;
		}

	}

	private class RetryHandlerImpl implements RetryHandler {

		DownloadTaskListener<?> proxied;
		int max;
		int current = 0;

		RetryHandlerImpl(DownloadTaskListener<?> proxied, int max) {
			this.proxied = proxied;
			this.max = max;
		}

		@Override
		public boolean doRetry(Throwable e) {
			current++;
			if (e instanceof IOException && current <= max) {
				proxied.retry(e, current, max);
				return true;
			}
			return false;
		}

	}

	private CloseableHttpAsyncClient httpClient;
	private ExecutorService bootstrapPool;
	private ReadWriteLock shutdownLock = new ReentrantReadWriteLock();
	private boolean shutdown = false;

	public HttpAsyncDownloader() {
		httpClient = HttpAsyncClientBuilder.create().setMaxConnPerRoute(DEFAULT_MAX_CONNECTIONS_PRE_ROUTER).setMaxConnTotal(DEFAULT_MAX_CONNECTIONS).build();
		bootstrapPool = new ThreadPoolExecutor(0, Runtime.getRuntime().availableProcessors(), 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		httpClient.start();
	}

	@Override
	public <T> Future<T> download(DownloadTask<T> task, DownloadTaskListener<T> listener) {
		if (listener == null) {
			listener = new NullDownloadTaskListener<>();
		}
		return download0(task, listener, null);
	}

	@Override
	public <T> Future<T> download(DownloadTask<T> task, DownloadTaskListener<T> listener, int retries) {
		if (listener == null) {
			listener = new NullDownloadTaskListener<>();
		}
		return download0(task, listener, new RetryHandlerImpl(listener, retries));
	}

	@Override
	public void shutdown() throws IOException {
		Lock lock = shutdownLock.writeLock();
		lock.lock();
		try {
			bootstrapPool.shutdownNow();
			httpClient.close();
		} finally {
			bootstrapPool = null;
			httpClient = null;
			lock.unlock();
		}
	}

	@Override
	public boolean isShutdown() {
		return shutdown;
	}

	private <T> Future<T> download0(DownloadTask<T> task, DownloadTaskListener<T> listener, RetryHandler retryHandler) {
		Lock lock = shutdownLock.readLock();
		lock.lock();
		try {
			if (shutdown) {
				throw new RejectedExecutionException("already shutdown");
			}
			TaskHandler<T> handler = new TaskHandler<>(task, listener, retryHandler);
			handler.start();
			return handler.futuer;
		} finally {
			lock.unlock();
		}
	}

}
