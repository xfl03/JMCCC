package com.github.to2mbn.jmccc.mcdownloader.download;

import java.util.Objects;

public class DownloadCallbackGroup<T> implements DownloadCallback<T> {

	@SafeVarargs
	public static <T> DownloadCallback<T> group(DownloadCallback<T>... callbacks) {
		return new DownloadCallbackGroup<>(callbacks);
	}

	private DownloadCallback<T>[] callbacks;

	public DownloadCallbackGroup(DownloadCallback<T>[] callbacks) {
		Objects.requireNonNull(callbacks);
		this.callbacks = callbacks;
	}

	@Override
	public void done(T result) {
		for (DownloadCallback<T> callback : callbacks) {
			callback.done(result);
		}
	}

	@Override
	public void failed(Throwable e) {
		for (DownloadCallback<T> callback : callbacks) {
			callback.failed(e);
		}
	}

	@Override
	public void cancelled() {
		for (DownloadCallback<T> callback : callbacks) {
			callback.cancelled();
		}
	}

	@Override
	public void updateProgress(long done, long total) {
		for (DownloadCallback<T> callback : callbacks) {
			callback.updateProgress(done, total);
		}
	}

	@Override
	public void retry(Throwable e, int current, int max) {
		for (DownloadCallback<T> callback : callbacks) {
			callback.retry(e, current, max);
		}
	}

}
