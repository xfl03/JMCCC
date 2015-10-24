package com.github.to2mbn.jmccc.mcdownloader.download.concurrent;

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
		for (AsyncCallback<T> callback : callbacks) {
			callback.done(result);
		}
	}

	@Override
	public void failed(Throwable e) {
		for (AsyncCallback<T> callback : callbacks) {
			callback.failed(e);
		}
	}

	@Override
	public void cancelled() {
		for (AsyncCallback<T> callback : callbacks) {
			callback.cancelled();
		}
	}

}
