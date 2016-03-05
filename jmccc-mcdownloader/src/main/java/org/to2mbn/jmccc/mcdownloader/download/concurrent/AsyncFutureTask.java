package org.to2mbn.jmccc.mcdownloader.download.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class AsyncFutureTask<V> extends FutureTask<V> {

	private volatile AsyncCallback<V> callback;

	public AsyncFutureTask(Callable<V> callable) {
		super(callable);
	}

	public AsyncFutureTask(Runnable runnable, V result) {
		super(runnable, result);
	}

	public AsyncCallback<V> getCallback() {
		return callback;
	}

	public void setCallback(AsyncCallback<V> callback) {
		this.callback = callback;
	}

	@Override
	protected void done() {
		AsyncCallback<V> c = callback;
		if (c != null) {
			V result;
			try {
				result = get();
			} catch (InterruptedException e) {
				throw new AssertionError(e);
			} catch (ExecutionException e) {
				Throwable exception = e.getCause();
				c.failed(exception);
				return;
			} catch (CancellationException e) {
				c.cancelled();
				return;
			}
			c.done(result);
		}
	}

}
