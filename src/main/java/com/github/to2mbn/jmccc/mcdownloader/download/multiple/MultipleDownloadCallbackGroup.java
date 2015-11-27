package com.github.to2mbn.jmccc.mcdownloader.download.multiple;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloadCallback;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloadCallbackGroup;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloadTask;

public class MultipleDownloadCallbackGroup<T> implements MultipleDownloadCallback<T> {

	@SafeVarargs
	public static <T> MultipleDownloadCallback<T> group(MultipleDownloadCallback<T>... callbacks) {
		return new MultipleDownloadCallbackGroup<>(callbacks);
	}

	private MultipleDownloadCallback<T>[] callbacks;

	public MultipleDownloadCallbackGroup(MultipleDownloadCallback<T>[] callbacks) {
		Objects.requireNonNull(callbacks);
		this.callbacks = callbacks;
	}

	@Override
	public void done(T result) {
		for (MultipleDownloadCallback<T> callback : callbacks) {
			callback.done(result);
		}
	}

	@Override
	public void failed(Throwable e) {
		for (MultipleDownloadCallback<T> callback : callbacks) {
			callback.failed(e);
		}
	}

	@Override
	public void cancelled() {
		for (MultipleDownloadCallback<T> callback : callbacks) {
			callback.cancelled();
		}
	}

	@Override
	public <R> DownloadCallback<R> taskStart(DownloadTask<R> task) {
		List<DownloadCallback<R>> listeners = new ArrayList<>();
		for (MultipleDownloadCallback<T> callback : callbacks) {
			DownloadCallback<R> listener = callback.taskStart(task);
			if (listener != null) {
				listeners.add(listener);
			}
		}
		@SuppressWarnings("unchecked")
		DownloadCallback<R>[] callbacksArray = listeners.toArray(new DownloadCallback[listeners.size()]);
		return listeners.isEmpty() ? null : new DownloadCallbackGroup<R>(callbacksArray);
	}

}
