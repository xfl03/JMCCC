package org.to2mbn.jmccc.mcdownloader.download.combine;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class BlockedMultipleCombinedTask extends CombinedDownloadTask<Object[]> {

    List<CombinedDownloadTask<?>> tasks;

    public BlockedMultipleCombinedTask(List<CombinedDownloadTask<?>> tasks) {
        this.tasks = tasks;
    }

    @Override
    public void execute(final CombinedDownloadContext<Object[]> context) throws Exception {
        Object[] results = new Object[tasks.size()];
        AtomicInteger doneTasks = new AtomicInteger(0);
        for (int i = 0; i < tasks.size(); ++i) {
            CombinedDownloadTask<?> task = tasks.get(i);
            if (task != null) {
                int finalI = i;
                task = task.andThen(it -> {
                    results[finalI] = it;
                    if (doneTasks.incrementAndGet() == tasks.size()) {
                        context.done(results);
                    }
                    return it;
                });
                context.submit(task, null, true);
            }
        }
    }

}