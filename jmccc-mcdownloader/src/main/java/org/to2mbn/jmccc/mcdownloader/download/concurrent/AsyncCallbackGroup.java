package org.to2mbn.jmccc.mcdownloader.download.concurrent;

import java.util.Objects;

public class AsyncCallbackGroup<T> implements AsyncCallback<T> {

	@SafeVarargs
	public static <T> AsyncCallback<T> group(AsyncCallback<T>... callbacks) {
		return new AsyncCallbackGroup<>(callbacks);
	}

	private AsyncCallback<T>[] callbacks;

	public AsyncCallbackGroup(AsyncCallback<T>[] callbacks) {
		Objects.requireNonNull(callbacks);
		this.callbacks = callbacks;
	}

	@Override
	public void done(T result) {
		RuntimeException ex = null;
		for (AsyncCallback<T> callback : callbacks) {
			try {
				callback.done(result);
			} catch (Throwable e) {
				if (ex == null) {
					ex = new RuntimeException();
				}
				ex.addSuppressed(e);
			}
		}
		if (ex != null) {
			throw ex;
		}
	}

	@Override
	public void failed(Throwable e) {
		RuntimeException ex1 = null;
		for (AsyncCallback<T> callback : callbacks) {
			try {
				callback.failed(e);
			} catch (Throwable e1) {
				if (ex1 == null) {
					ex1 = new RuntimeException();
				}
				ex1.addSuppressed(e1);
			}
		}
		if (ex1 != null) {
			throw ex1;
		}
	}

	@Override
	public void cancelled() {
		RuntimeException ex = null;
		for (AsyncCallback<T> callback : callbacks) {
			try {
				callback.cancelled();
			} catch (Throwable e) {
				if (ex == null) {
					ex = new RuntimeException();
				}
				ex.addSuppressed(e);
			}
		}
		if (ex != null) {
			throw ex;
		}
	}

}
