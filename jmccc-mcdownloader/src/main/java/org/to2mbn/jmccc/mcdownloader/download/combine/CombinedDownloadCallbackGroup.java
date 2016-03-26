package org.to2mbn.jmccc.mcdownloader.download.combine;

import java.util.ArrayList;
import java.util.List;
import org.to2mbn.jmccc.mcdownloader.download.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.DownloadCallbacks;
import org.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.CallbackGroup;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.EventDispatchException;

class CombinedDownloadCallbackGroup<T> extends CallbackGroup<T> implements CombinedDownloadCallback<T> {

	private CombinedDownloadCallback<T>[] callbacks;

	public CombinedDownloadCallbackGroup(CombinedDownloadCallback<T>[] callbacks) {
		super(callbacks);
		this.callbacks = callbacks;
	}

	@Override
	public <R> DownloadCallback<R> taskStart(DownloadTask<R> task) {
		List<DownloadCallback<R>> listeners = new ArrayList<>();
		EventDispatchException ex = null;
		for (CombinedDownloadCallback<T> callback : callbacks) {
			DownloadCallback<R> listener = null;
			try {
				listener = callback.taskStart(task);
			} catch (Throwable e) {
				if (ex == null) {
					ex = new EventDispatchException();
				} else {
					ex.addSuppressed(e);
				}
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
		return listeners.isEmpty() ? null : DownloadCallbacks.group(callbacksArray);
	}

}
