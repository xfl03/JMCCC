package org.to2mbn.jmccc.mcdownloader.download.combine;

import org.to2mbn.jmccc.mcdownloader.download.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.DownloadTask;

class NullCombinedDownloadCallback<T> implements CombinedDownloadCallback<T> {

	@Override
	public void done(T result) {
	}

	@Override
	public void failed(Throwable e) {
	}

	@Override
	public void cancelled() {
	}

	@Override
	public <R> DownloadCallback<R> taskStart(DownloadTask<R> task) {
		return null;
	}

}