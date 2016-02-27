package org.to2mbn.jmccc.mcdownloader.download.combine;

class MultipleCombinedDownloadTask extends CombinedDownloadTask<Void> {

	CombinedDownloadTask<?>[] tasks;

	public MultipleCombinedDownloadTask(CombinedDownloadTask<?>[] tasks) {
		this.tasks = tasks;
	}

	@Override
	public void execute(final CombinedDownloadContext<Void> context) throws Exception {
		for (CombinedDownloadTask<?> task : tasks) {
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