package org.to2mbn.jmccc.mcdownloader.download.combine;

import org.to2mbn.jmccc.mcdownloader.download.DownloadTask;

class MultipleCombinedDownloadTask extends CombinedDownloadTask<Object> {

	DownloadTask<?>[] tasks;

	public MultipleCombinedDownloadTask(DownloadTask<?>[] tasks) {
		this.tasks = tasks;
	}

	@Override
	public void execute(final CombinedDownloadContext<Object> context) throws Exception {
		for (DownloadTask<?> task : tasks) {
			if (task == null) {
				context.submit(task, null, true);
			}
		}
		context.awaitAllTasks(new Runnable() {

			@Override
			public void run() {
				context.done(null);
			}
		});
	}

}