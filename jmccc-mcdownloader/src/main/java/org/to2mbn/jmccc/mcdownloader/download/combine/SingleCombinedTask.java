package org.to2mbn.jmccc.mcdownloader.download.combine;

import org.to2mbn.jmccc.mcdownloader.download.concurrent.CallbackAdapter;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;

class SingleCombinedTask<T> extends CombinedDownloadTask<T> {

    DownloadTask<T> task;

    public SingleCombinedTask(DownloadTask<T> task) {
        this.task = task;
    }

    @Override
    public void execute(final CombinedDownloadContext<T> context) throws Exception {
        context.submit(task, new CallbackAdapter<T>() {

            @Override
            public void done(T result) {
                context.done(result);
            }

        }, true);
    }

}