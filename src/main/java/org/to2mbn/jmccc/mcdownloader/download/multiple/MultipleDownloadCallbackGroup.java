package org.to2mbn.jmccc.mcdownloader.download.multiple;

import java.util.ArrayList;
import java.util.List;
import org.to2mbn.jmccc.mcdownloader.download.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.DownloadCallbackGroup;
import org.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.AsyncCallbackGroup;

public class MultipleDownloadCallbackGroup<T> extends AsyncCallbackGroup<T> implements MultipleDownloadCallback<T> {

	@SafeVarargs
	public static <T> MultipleDownloadCallback<T> group(MultipleDownloadCallback<T>... callbacks) {
		return new MultipleDownloadCallbackGroup<>(callbacks);
	}

	private MultipleDownloadCallback<T>[] callbacks;

	public MultipleDownloadCallbackGroup(MultipleDownloadCallback<T>[] callbacks) {
		super(callbacks);
		this.callbacks = callbacks;
	}

	@Override
	public <R> DownloadCallback<R> taskStart(DownloadTask<R> task) {
		List<DownloadCallback<R>> listeners = new ArrayList<>();
		RuntimeException ex = null;
		for (MultipleDownloadCallback<T> callback : callbacks) {
			DownloadCallback<R> listener = null;
			try {
				listener = callback.taskStart(task);
			} catch (Throwable e) {
				if (ex == null) {
					ex = new RuntimeException();
				}
				ex.addSuppressed(e);
			}
			if (listener != null) {
				listeners.add(listener);
			}
		}
		if (ex != null) {
			throw ex;
		}
		@SuppressWarnings("unchecked")
		DownloadCallback<R>[] callbacksArray = listeners.toArray(new DownloadCallback[listeners.size()]);
		return listeners.isEmpty() ? null : new DownloadCallbackGroup<R>(callbacksArray);
	}

}
