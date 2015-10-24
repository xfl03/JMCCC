package com.github.to2mbn.jmccc.mcdownloader.download;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
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

	private class TaskHandler<T> implements Runnable, Cancellable {

		private class LifeCycleHandler<R> {

			private AsyncCallback<R> proxied;

			public LifeCycleHandler(AsyncCallback<R> proxied) {
				this.proxied = proxied;
			}

			public void failed(Throwable e) {
				if (session != null) {
					try {
						session.failed(e);
					} catch (Throwable e1) {
						if (e != e1) {
							e.addSuppressed(e1);
						}
					}
				}
				proxied.failed(e);
			}

			public void cancelled() {
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

			public void done(R result) {
				proxied.done(result);
			}

		}

		DownloadTask<T> task;
		AsyncFuture<T> futuer;
		LifeCycleHandler<T> lifecycle;
		DownloadSession<T> session;
		DownloadTaskListener<T> listener;
		Future<T> downloadFuture;
		volatile boolean cancelled;
		volatile boolean mayInterruptIfRunning;

		TaskHandler(DownloadTask<T> task, DownloadTaskListener<T> listener) {
			this.task = task;
			this.listener = listener;
			this.futuer = new AsyncFuture<>(this);
			this.lifecycle = new LifeCycleHandler<>(AsyncCallbackGroup.group(futuer, listener));
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

	private CloseableHttpAsyncClient httpClient;
	private ExecutorService bootstrapPool;
	private ReadWriteLock shutdownLock = new ReentrantReadWriteLock();
	private boolean shutdown = false;

	public HttpAsyncDownloader() {
		httpClient = HttpAsyncClientBuilder.create().build();
		bootstrapPool = new ThreadPoolExecutor(0, Runtime.getRuntime().availableProcessors(), 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
		httpClient.start();
	}

	@Override
	public <T> Future<T> download(DownloadTask<T> task, DownloadTaskListener<T> listener) {
		if (listener == null) {
			listener = new DownloadTaskListener<T>() {

				@Override
				public void done(T result) {
				}

				@Override
				public void failed(Throwable e) {
					e.printStackTrace();
				}

				@Override
				public void cancelled() {
				}

				@Override
				public void updateProgress(long done, long total) {
				}
			};
		}
		Lock lock = shutdownLock.readLock();
		lock.lock();
		try {
			if (shutdown) {
				throw new IllegalStateException("already shutdown");
			}
			TaskHandler<T> handler = new TaskHandler<>(task, listener);
			handler.start();
			return handler.futuer;
		} finally {
			lock.unlock();
		}
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

}
