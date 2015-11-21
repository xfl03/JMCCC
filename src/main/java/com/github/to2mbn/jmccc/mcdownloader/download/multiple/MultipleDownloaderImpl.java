package com.github.to2mbn.jmccc.mcdownloader.download.multiple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloadCallback;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloadCallbackGroup;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import com.github.to2mbn.jmccc.mcdownloader.download.Downloader;
import com.github.to2mbn.jmccc.mcdownloader.download.concurrent.AsyncCallback;
import com.github.to2mbn.jmccc.mcdownloader.download.concurrent.AsyncCallbackGroup;
import com.github.to2mbn.jmccc.mcdownloader.download.concurrent.AsyncFuture;
import com.github.to2mbn.jmccc.mcdownloader.download.concurrent.Cancellable;

public class MultipleDownloaderImpl implements MultipleDownloader {

	private static class NullMultipleDownloadCallback<T> implements MultipleDownloadCallback<T> {

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
		public <R> DownloadCallback<R> taskStart(DownloadTask<R> task) {
			return null;
		}

	}

	private class TaskHandler<T> implements Cancellable, Runnable {

		AsyncFuture<T> future;
		MultipleDownloadTask<T> task;
		MultipleDownloadCallback<T> callback;
		int tries;
		Set<Future<?>> activeSubtasks = Collections.newSetFromMap(new ConcurrentHashMap<Future<?>, Boolean>());
		ReadWriteLock rwlock = new ReentrantReadWriteLock();
		MultipleDownloadContext<T> context;
		AsyncCallback<T> groupcallback;
		volatile Future<?> mainfuture;
		volatile boolean terminated = false;
		volatile boolean cancelled = false;
		volatile int activeTasksCount = 0;
		volatile List<Runnable> activeTasksCountZeroCallbacks = new ArrayList<>();
		Object activeTasksCountLock = new Object();

		TaskHandler(MultipleDownloadTask<T> task, MultipleDownloadCallback<T> callback, int tries) {
			this.task = task;
			this.callback = callback;
			this.tries = tries;
			future = new AsyncFuture<>(this);
			groupcallback = AsyncCallbackGroup.group(future, callback);
			context = new MultipleDownloadContext<T>() {

				@Override
				public void done(T result) {
					lifecycleDone(result);
				}

				@Override
				public void failed(Throwable e) {
					lifecycleFailed(e);
				}

				@Override
				public void cancelled() {
					lifecycleCancel();
				}

				@Override
				public Future<?> submit(final Runnable task, final AsyncCallback<?> taskcallback, boolean fatal) throws InterruptedException {
					return submit(new Callable<Object>() {

						@Override
						public Object call() throws Exception {
							task.run();
							return null;
						}
					}, new AsyncCallback<Object>() {

						@Override
						public void done(Object result) {
							taskcallback.done(null);
						}

						@Override
						public void failed(Throwable e) {
							taskcallback.failed(e);
						}

						@Override
						public void cancelled() {
							taskcallback.cancelled();
						}
					}, fatal);
				}

				@Override
				public <R> Future<R> submit(final Callable<R> task, final AsyncCallback<R> taskcallback, final boolean fatal) throws InterruptedException {
					Lock lock = rwlock.readLock();
					lock.lock();
					try {
						checkInterrupt();
						activeTasksCountup();
						Future<R> future = executor.submit(new Callable<R>() {

							@Override
							public R call() throws Exception {
								R val;
								try {
									val = task.call();
								} catch (InterruptedException e) {
									if (fatal) {
										lifecycleCancel();
									}
									if (taskcallback != null) {
										taskcallback.cancelled();
									}
									throw e;
								} catch (Throwable e) {
									if (fatal) {
										lifecycleFailed(e);
									}
									if (taskcallback != null) {
										taskcallback.failed(e);
									}
									throw e;
								} finally {
									activeTasksCountdown();
								}
								if (taskcallback != null) {
									taskcallback.done(val);
								}
								return val;
							}
						});
						activeSubtasks.add(future);
						return future;
					} finally {
						lock.unlock();
					}
				}

				@Override
				public <R> Future<R> submit(DownloadTask<R> task, DownloadCallback<R> taskcallback, boolean fatal) throws InterruptedException {
					Lock lock = rwlock.readLock();
					lock.lock();
					try {
						checkInterrupt();

						DownloadCallback<R> outsidecallback = TaskHandler.this.callback.taskStart(task);
						List<DownloadCallback<R>> callbacks = new ArrayList<>();
						callbacks.add(new DownloadCallback<R>() {

							@Override
							public void done(R result) {
								activeTasksCountdown();
							}

							@Override
							public void failed(Throwable e) {
								activeTasksCountdown();
							}

							@Override
							public void cancelled() {
								activeTasksCountdown();
							}

							@Override
							public void updateProgress(long done, long total) {
							}

							@Override
							public void retry(Throwable e, int current, int max) {
							}
						});
						if (fatal) {
							callbacks.add(new DownloadCallback<R>() {

								@Override
								public void done(R result) {
								}

								@Override
								public void failed(Throwable e) {
									lifecycleFailed(e);
								}

								@Override
								public void cancelled() {
									lifecycleCancel();
								}

								@Override
								public void updateProgress(long done, long total) {
								}

								@Override
								public void retry(Throwable e, int current, int max) {
								}
							});
						}
						if (taskcallback != null) {
							callbacks.add(taskcallback);
						}
						if (outsidecallback != null) {
							callbacks.add(outsidecallback);
						}
						@SuppressWarnings("unchecked")
						DownloadCallback<R>[] callbacksArray = new DownloadCallback[callbacks.size()];
						activeTasksCountup();
						Future<R> taskfuture = downloader.download(task, new DownloadCallbackGroup<R>(callbacksArray), TaskHandler.this.tries);
						activeSubtasks.add(taskfuture);
						return taskfuture;
					} finally {
						lock.unlock();
					}
				}

				void checkInterrupt() throws InterruptedException {
					if (Thread.interrupted() || cancelled) {
						throw new InterruptedException();
					}
				}

				@Override
				public void awaitAllTasks(Runnable callback) throws InterruptedException {
					synchronized (activeTasksCountLock) {
						if (activeTasksCount != 0) {
							activeTasksCountZeroCallbacks.add(callback);
							return;
						}
					}
					callback.run();
				}

				void activeTasksCountup() {
					synchronized (activeTasksCountLock) {
						activeTasksCount++;
					}
				}

				void activeTasksCountdown() {
					List<Runnable> callbacks = null;
					synchronized (activeTasksCountLock) {
						activeTasksCount--;
						if (activeTasksCount < 0) {
							throw new IllegalStateException("illegal active tasks count: " + activeTasksCount);
						}
						if (activeTasksCount == 0) {
							callbacks = activeTasksCountZeroCallbacks;
							activeTasksCountZeroCallbacks = new ArrayList<>();
						}
					}
					if (callbacks != null) {
						for (Runnable callback : callbacks) {
							callback.run();
						}
					}
				}
			};
		}

		void start() {
			mainfuture = executor.submit(this);
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			if (!cancelled) {
				Lock lock = rwlock.writeLock();
				lock.lock();
				try {
					cancelled = true;
				} finally {
					lock.unlock();
				}
				if (mainfuture != null) {
					mainfuture.cancel(true);
				}
				for (Future<?> subfuture : activeSubtasks) {
					subfuture.cancel(true);
				}
			}
			return true;
		}

		@Override
		public void run() {
			if (cancelled) {
				lifecycleCancel();
				return;
			}
			try {
				task.execute(context);
			} catch (InterruptedException e) {
				lifecycleCancel();
			} catch (Throwable e) {
				lifecycleFailed(e);
			}
		}

		void lifecycleDone(T result) {
			if (!terminated) {
				Lock lock = rwlock.writeLock();
				lock.lock();
				try {
					if (!terminated) {
						groupcallback.done(result);
						terminated = true;
					}
				} finally {
					lock.unlock();
				}
			}
		}

		void lifecycleFailed(Throwable ex) {
			if (!terminated) {
				Lock lock = rwlock.writeLock();
				lock.lock();
				try {
					if (!terminated) {
						cancel(true);
						groupcallback.failed(ex);
						terminated = true;
					}
				} finally {
					lock.unlock();
				}
			}
		}

		void lifecycleCancel() {
			if (!terminated) {
				Lock lock = rwlock.writeLock();
				lock.lock();
				try {
					if (!terminated) {
						cancel(true);
						groupcallback.cancelled();
						terminated = true;
					}
				} finally {
					lock.unlock();
				}
			}
		}

	}

	private ExecutorService executor;
	private Downloader downloader;

	public MultipleDownloaderImpl(ExecutorService executor, Downloader downloader) {
		this.executor = executor;
		this.downloader = downloader;
	}

	@Override
	public <T> Future<T> download(MultipleDownloadTask<T> task, MultipleDownloadCallback<T> callback, int tries) {
		Objects.requireNonNull(task);
		if (tries < 1) {
			throw new IllegalArgumentException("tries < 1");
		}
		TaskHandler<T> handler = new TaskHandler<>(task, callback == null ? new NullMultipleDownloadCallback<T>() : callback, tries);
		handler.start();
		return handler.future;
	}

}
