package org.to2mbn.jmccc.mcdownloader.download.concurrent;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

abstract public class CallbackAsyncFutureTask<V> implements RunnableFuture<V>, Cancellable {

	private static class InterruptedExceptionMapper<V> implements Callback<V> {

		private final Callback<V> mapped;

		public InterruptedExceptionMapper(Callback<V> mapped) {
			this.mapped = mapped;
		}

		@Override
		public void done(V result) {
			mapped.done(result);
		}

		@Override
		public void failed(Throwable e) {
			if (e instanceof InterruptedException) {
				mapped.cancelled();
			} else {
				mapped.failed(e);
			}
		}

		@Override
		public void cancelled() {
			mapped.cancelled();
		}

	}

	private static class ThreadCancellableAdapter implements Cancellable {

		private final Thread t;

		public ThreadCancellableAdapter(Thread t) {
			this.t = t;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			t.interrupt();
			return true;
		}

	}

	private class CancelProcesser implements Cancellable {

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			for (Object cancellable : cancellables) {
				if (cancellable instanceof Future) {
					((Future<?>) cancellable).cancel(mayInterruptIfRunning);
				} else if (cancellable instanceof Cancellable) {
					((Cancellable) cancellable).cancel(mayInterruptIfRunning);
				}
			}
			return true;
		}

	}

	private final Set<Object> cancellables = Collections.newSetFromMap(new ConcurrentHashMap<Object, Boolean>());
	private final AsyncFuture<V> future;
	private final Callback<V> lifecycle;

	private final AtomicBoolean running = new AtomicBoolean(false);

	public CallbackAsyncFutureTask() {
		future = new AsyncFuture<>(new CancelProcesser());
		lifecycle = new InterruptedExceptionMapper<V>(future);
	}

	public Callback<V> getCallback() {
		return future.getCallback();
	}

	public void setCallback(Callback<V> callback) {
		future.setCallback(callback);
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		return future.get();
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return future.get(timeout, unit);
	}

	@Override
	public boolean isCancelled() {
		return future.isCancelled();
	}

	@Override
	public boolean isDone() {
		return future.isDone();
	}

	@Override
	public void run() {
		if (!future.isCancelled() && running.compareAndSet(false, true)) {
			Cancellable canceller = new ThreadCancellableAdapter(Thread.currentThread());
			addCancellable(canceller);
			try {
				if (future.isCancelled()) {
					return;
				}
				try {
					execute();
				} catch (Throwable e) {
					lifecycle.failed(e);
				}
			} finally {
				removeCancellable(canceller);
			}
		}
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return future.cancel(mayInterruptIfRunning);
	}

	abstract protected void execute() throws Exception;

	protected Callback<V> lifecycle() {
		return lifecycle;
	}

	protected void addCancellable(Cancellable cancelable) {
		cancellables.add(cancelable);
	}

	protected void addCancellable(Future<?> cancelable) {
		cancellables.add(cancelable);
	}

	protected void removeCancellable(Cancellable cancelable) {
		cancellables.remove(cancelable);
	}

	protected void removeCancellable(Future<?> cancelable) {
		cancellables.remove(cancelable);
	}

}
