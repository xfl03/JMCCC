package org.to2mbn.jmccc.mcdownloader.download.combine;

import java.util.concurrent.Callable;

class MultipleCombinedTask extends CombinedDownloadTask<Void> {

    CombinedDownloadTask<?>[] tasks;

    public MultipleCombinedTask(CombinedDownloadTask<?>[] tasks) {
        this.tasks = tasks;
    }

    @Override
    public void execute(final CombinedDownloadContext<Void> context) throws Exception {
        for (CombinedDownloadTask<?> task : tasks) {
            if (task != null) {
                context.submit(task, null, true);
            }
        }
        context.awaitAllTasks(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                context.done(null);
                return null;
            }
        });
    }

}