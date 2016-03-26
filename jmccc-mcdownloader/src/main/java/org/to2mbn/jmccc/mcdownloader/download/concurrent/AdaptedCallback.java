package org.to2mbn.jmccc.mcdownloader.download.concurrent;

import java.util.Objects;
import org.to2mbn.jmccc.mcdownloader.download.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadCallback;

public class AdaptedCallback<V> implements Callback<V>, DownloadCallback<V>, CombinedDownloadCallback<V> {

	private final Callback<V> adapted;

	public AdaptedCallback(Callback<V> adapted) {
		Objects.requireNonNull(adapted);
		this.adapted = adapted;
	}

	@Override
	public void done(V result) {
		adapted.done(result);
	}

	@Override
	public void failed(Throwable e) {
		adapted.failed(e);
	}

	@Override
	public void cancelled() {
		adapted.cancelled();
	}

	@Override
	public <R> DownloadCallback<R> taskStart(DownloadTask<R> task) {
		return null;
	}

	@Override
	public void updateProgress(long done, long total) {

	}

	@Override
	public void retry(Throwable e, int current, int max) {

	}

}
