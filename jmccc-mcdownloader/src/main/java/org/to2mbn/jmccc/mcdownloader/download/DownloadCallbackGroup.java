package org.to2mbn.jmccc.mcdownloader.download;

import org.to2mbn.jmccc.mcdownloader.download.concurrent.CallbackGroup;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.EventDispatchException;

class DownloadCallbackGroup<T> extends CallbackGroup<T> implements DownloadCallback<T> {

	private DownloadCallback<T>[] callbacks;

	public DownloadCallbackGroup(DownloadCallback<T>[] callbacks) {
		super(callbacks);
		this.callbacks = callbacks;
	}

	@Override
	public void updateProgress(long done, long total) {
		EventDispatchException ex = null;
		for (DownloadCallback<T> callback : callbacks) {
			try {
				callback.updateProgress(done, total);
			} catch (Throwable e) {
				if (ex == null) {
					ex = new EventDispatchException();
				}
				ex.addSuppressed(e);
			}
		}
		if (ex != null) {
			throw ex;
		}
	}

	@Override
	public void retry(Throwable e, int current, int max) {
		EventDispatchException ex1 = null;
		for (DownloadCallback<T> callback : callbacks) {
			try {
				callback.retry(e, current, max);
			} catch (Throwable e1) {
				if (ex1 == null) {
					ex1 = new EventDispatchException();
				}
				ex1.addSuppressed(e1);
			}
		}
		if (ex1 != null) {
			throw ex1;
		}
	}

}
