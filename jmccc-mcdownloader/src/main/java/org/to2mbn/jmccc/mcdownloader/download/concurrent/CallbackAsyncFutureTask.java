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

abstract public class CallbackAsyncFutureTask<V> implements RunnableFuture<V>, Cancelable {

	private static void cancelCancelable(Object cancelable, boolean mayInterruptIfRunning) {
		if (cancelable instanceof Future) {
			((Future<?>) cancelable).cancel(mayInterruptIfRunning);
		} else if (cancelable instanceof Cancelable) {
			((Cancelable) cancelable).cancel(mayInterruptIfRunning);
		}
	}

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

	private static class ThreadCancelableAdapter implements Cancelable {

		private final Thread t;

		public ThreadCancelableAdapter(Thread t) {
			this.t = t;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			t.interrupt();
			return true;
		}

	}

	private class CancelProcesser implements Cancelable {

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			for (Object cancelable : cancelables) {
				cancelCancelable(cancelable, mayInterruptIfRunning);
				cancelables.remove(cancelable);
			}
			return true;
		}

	}

	private final Set<Object> cancelables = Collections.newSetFromMap(new ConcurrentHashMap<Object, Boolean>());
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
			Cancelable canceller = new ThreadCancelableAdapter(Thread.currentThread());
			addCancelable(canceller);
			try {
				if (Thread.interrupted())
					return;

				try {
					execute();
				} catch (Throwable e) {
					lifecycle.failed(e);
				}
			} finally {
				removeCancelable(canceller);
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

	protected void addCancelable(Cancelable cancelable) {
		cancelables.add(cancelable);
		cancelIfNecessary(cancelable);
	}

	protected void addCancelable(Future<?> cancelable) {
		cancelables.add(cancelable);
		cancelIfNecessary(cancelable);
	}

	protected void removeCancelable(Cancelable cancelable) {
		cancelables.remove(cancelable);
	}

	protected void removeCancelable(Future<?> cancelable) {
		cancelables.remove(cancelable);
	}

	private void cancelIfNecessary(Object cancelable) {
		if (future.isCancelled()) {
			cancelCancelable(cancelable, true);
			cancelables.remove(cancelable);
		}
	}

}
