package org.to2mbn.jmccc.mcdownloader.download.combine;

import org.to2mbn.jmccc.mcdownloader.download.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.DownloadTask;

class SingleCombinedDownloadTask<U> extends CombinedDownloadTask<U> {

	DownloadTask<U> task;

	public SingleCombinedDownloadTask(DownloadTask<U> task) {
		this.task = task;
	}

	@Override
	public void execute(final CombinedDownloadContext<U> context) throws Exception {
		context.submit(task, new DownloadCallback<U>() {

			@Override
			public void done(U result) {
				context.done(result);
			}

			@Override
			public void failed(Throwable e) {
			}

			@Override
			public void cancelled() {
			}

			@Override
			public void updateProgress(long done, long total) {
			}

			@Override
			public void retry(Throwable e, int current, int max) {
			}
		}, true);
	}

}