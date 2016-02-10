package org.to2mbn.jmccc.mcdownloader.download.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AsyncFuture<T> implements Future<T>, AsyncCallback<T> {

	private static final int RUNNING = 0;
	private static final int DONE = 1;
	private static final int FAILED = 2;
	private static final int CANCELLED = 3;

	private volatile int state = RUNNING;
	private volatile Throwable e;
	private volatile T result;
	private CountDownLatch latch = new CountDownLatch(1);
	private Cancellable cancellable;
	private Object stateLock = new Object();

	public AsyncFuture() {
		this(null);
	}

	public AsyncFuture(Cancellable cancellable) {
		this.cancellable = cancellable;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (cancellable != null && state == RUNNING) {
			return cancellable.cancel(mayInterruptIfRunning);
		}
		return false;
	}

	@Override
	public boolean isCancelled() {
		return state == CANCELLED;
	}

	@Override
	public boolean isDone() {
		return state == DONE;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		if (state == RUNNING) {
			latch.await();
		}
		return getResult();
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		if (state == RUNNING) {
			if (!latch.await(timeout, unit)) {
				throw new TimeoutException();
			}
		}
		return getResult();
	}

	@Override
	public void done(T result) {
		synchronized (stateLock) {
			checkUpdateState();
			this.result = result;
			state = DONE;
		}
		terminated();
	}

	@Override
	public void failed(Throwable e) {
		synchronized (stateLock) {
			checkUpdateState();
			this.e = e;
			state = FAILED;
		}
		terminated();
	}

	@Override
	public void cancelled() {
		synchronized (stateLock) {
			checkUpdateState();
			state = CANCELLED;
		}
		terminated();
	}

	private void checkUpdateState() {
		if (state != RUNNING) {
			throw new IllegalStateException("task already terminated");
		}
	}

	private void terminated() {
		latch.countDown();
	}

	private T getResult() throws ExecutionException {
		switch (state) {
			case DONE:
				return result;

			case FAILED:
				throw new ExecutionException(e);

			case CANCELLED:
				throw new CancellationException();

			default:
				throw new IllegalStateException("not in a completed state");
		}
	}

}
