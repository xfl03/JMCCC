package org.to2mbn.jmccc.mcdownloader.download.combine;

import org.to2mbn.jmccc.mcdownloader.download.AbstractDownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.DownloadTask;

class SingleCombinedDownloadTask<T> extends CombinedDownloadTask<T> {

	DownloadTask<T> task;

	public SingleCombinedDownloadTask(DownloadTask<T> task) {
		this.task = task;
	}

	@Override
	public void execute(final CombinedDownloadContext<T> context) throws Exception {
		context.submit(task, new AbstractDownloadCallback<T>() {

			@Override
			public void done(T result) {
				context.done(result);
			}

		}, true);
	}

}