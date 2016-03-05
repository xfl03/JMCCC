package org.to2mbn.jmccc.mcdownloader.download.combine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.to2mbn.jmccc.mcdownloader.download.AbstractDownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.DownloadCallbackGroup;
import org.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.Downloader;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.CallbackAdapter;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.Callback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.CallbackGroup;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.AsyncFuture;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.Cancellable;

public class CombinedDownloaderImpl implements CombinedDownloader {

	private class TaskHandler<T> implements Cancellable, Runnable {

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
			}

		}

		AsyncFuture<T> future;
		CombinedDownloadTask<T> task;
		CombinedDownloadCallback<T> callback;
		int tries;
		Set<Future<?>> activeSubtasks = Collections.newSetFromMap(new ConcurrentHashMap<Future<?>, Boolean>());
		ReadWriteLock rwlock = new ReentrantReadWriteLock();
		CombinedDownloadContext<T> context;
		Callback<T> groupcallback;
		volatile Future<?> mainfuture;
		volatile boolean terminated = false;
		volatile boolean cancelled = false;
		volatile int activeTasksCount = 0;
		volatile List<Runnable> activeTasksCountZeroCallbacks = new ArrayList<>();
		Object activeTasksCountLock = new Object();

		TaskHandler(CombinedDownloadTask<T> task, CombinedDownloadCallback<T> callback, int tries) {
			this.task = task;
			this.callback = callback;
			this.tries = tries;
			future = new AsyncFuture<>(this);
			groupcallback = CallbackGroup.group(new Inactiver(), future, callback);
			context = new CombinedDownloadContext<T>() {

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
				public Future<?> submit(final Runnable task, final Callback<?> taskcallback, boolean fatal) throws InterruptedException {
					return submit(new Callable<Void>() {

						@Override
						public Void call() throws Exception {
							task.run();
							return null;
						}
					}, new Callback<Void>() {

						@Override
						public void done(Void result) {
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
				public <R> Future<R> submit(Callable<R> task, Callback<R> taskcallback, boolean fatal) throws InterruptedException {
					List<Callback<R>> callbacks = new ArrayList<>();
					callbacks.add(new Callback<R>() {

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
					});
					if (fatal) {
						callbacks.add(new CallbackAdapter<R>() {

							@Override
							public void failed(Throwable e) {
								lifecycleFailed(e);
							}

							@Override
							public void cancelled() {
								lifecycleCancel();
							}
						});
					}
					if (taskcallback != null) {
						callbacks.add(taskcallback);
					}
					CallableTaskHandler<R> handler = new CallableTaskHandler<>(task, callbacks, executor);

					Lock lock = rwlock.readLock();
					lock.lock();
					try {
						checkInterrupt();
						activeTasksCountup();
						activeSubtasks.add(handler.future);
					} finally {
						lock.unlock();
					}

					handler.run();
					return handler.future;
				}

				@Override
				public <R> Future<R> submit(DownloadTask<R> task, DownloadCallback<R> taskcallback, boolean fatal) throws InterruptedException {
					Lock lock = rwlock.readLock();
					lock.lock();
					Lock globalLock = shutdownLock.readLock();
					globalLock.lock();
					try {
						checkInterrupt();

						DownloadCallback<R> outsidecallback = TaskHandler.this.callback.taskStart(task);
						List<DownloadCallback<R>> callbacks = new ArrayList<>();
						if (fatal) {
							callbacks.add(new AbstractDownloadCallback<R>() {

								@Override
								public void failed(Throwable e) {
									lifecycleFailed(e);
								}

								@Override
								public void cancelled() {
									lifecycleCancel();
								}

							});
						}
						if (taskcallback != null) {
							callbacks.add(taskcallback);
						}
						if (outsidecallback != null) {
							callbacks.add(outsidecallback);
						}
						callbacks.add(new AbstractDownloadCallback<R>() {

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

						});

						@SuppressWarnings("unchecked")
						DownloadCallback<R>[] callbacksArray = callbacks.toArray(new DownloadCallback[callbacks.size()]);
						activeTasksCountup();
						Future<R> taskfuture = downloader.download(task, new DownloadCallbackGroup<R>(callbacksArray), TaskHandler.this.tries);
						activeSubtasks.add(taskfuture);
						return taskfuture;
					} finally {
						globalLock.unlock();
						lock.unlock();
					}
				}

				@Override
				public <R> Future<R> submit(CombinedDownloadTask<R> task, CombinedDownloadCallback<R> callback, boolean fatal) throws InterruptedException {
					Lock lock = rwlock.readLock();
					lock.lock();
					Lock globalLock = shutdownLock.readLock();
					globalLock.lock();
					try {
						checkInterrupt();

						List<CombinedDownloadCallback<R>> callbacks = new ArrayList<>();

						if (fatal) {
							callbacks.add(new AbstractCombinedDownloadCallback<R>() {

								@Override
								public void failed(Throwable e) {
									lifecycleFailed(e);
								}

								@Override
								public void cancelled() {
									lifecycleCancel();
								}

							});
						}

						if (callback != null) {
							callbacks.add(callback);
						}

						callbacks.add(new CombinedDownloadCallback<R>() {

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
							public <S> DownloadCallback<S> taskStart(DownloadTask<S> task) {
								return TaskHandler.this.callback.taskStart(task);
							}

						});

						@SuppressWarnings("unchecked")
						CombinedDownloadCallback<R>[] callbacksArray = callbacks.toArray(new CombinedDownloadCallback[callbacks.size()]);
						activeTasksCountup();
						Future<R> taskfuture = download(task, new CombinedDownloadCallbackGroup<>(callbacksArray), TaskHandler.this.tries);
						activeSubtasks.add(taskfuture);
						return taskfuture;
					} finally {
						globalLock.unlock();
						lock.unlock();
					}
				}

				void checkInterrupt() throws InterruptedException {
					if (Thread.interrupted() || cancelled || shutdown) {
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
			lifecycleCancel();
			return true;
		}

		void doCancel() {
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
				lifecycleCancel();
			}
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
						terminated = true;
						groupcallback.done(result);
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
						terminated = true;
						doCancel();
						groupcallback.failed(ex);
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
						terminated = true;
						doCancel();
						groupcallback.cancelled();
					}
				} finally {
					lock.unlock();
				}
			}
		}

	}

	private ExecutorService executor;
	private Downloader downloader;
	private Set<TaskHandler<?>> activeTasks = Collections.newSetFromMap(new ConcurrentHashMap<TaskHandler<?>, Boolean>());
	private volatile boolean shutdown = false;
	private ReadWriteLock shutdownLock = new ReentrantReadWriteLock();

	public CombinedDownloaderImpl(ExecutorService executor, Downloader downloader) {
		this.executor = executor;
		this.downloader = downloader;
	}

	@Override
	public <T> Future<T> download(CombinedDownloadTask<T> task, CombinedDownloadCallback<T> callback) {
		return download(task, callback, 1);
	}

	@Override
	public <T> Future<T> download(CombinedDownloadTask<T> task, CombinedDownloadCallback<T> callback, int tries) {
		Objects.requireNonNull(task);
		if (tries < 1) {
			throw new IllegalArgumentException("tries < 1");
		}
		Lock lock = shutdownLock.readLock();
		lock.lock();
		try {
			if (shutdown) {
				throw new RejectedExecutionException("already shutdown");
			}
			TaskHandler<T> handler = new TaskHandler<>(task, callback == null ? new NullCombinedDownloadCallback<T>() : callback, tries);
			activeTasks.add(handler);
			handler.start();
			return handler.future;
		} finally {
			lock.unlock();
		}
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

			for (TaskHandler<?> task : activeTasks) {
				task.cancel(true);
			}
		}
	}

	@Override
	public boolean isShutdown() {
		return shutdown;
	}

}
