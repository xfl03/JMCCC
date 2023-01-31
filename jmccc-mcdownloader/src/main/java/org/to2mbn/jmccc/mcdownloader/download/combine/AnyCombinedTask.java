package org.to2mbn.jmccc.mcdownloader.download.combine;

import org.to2mbn.jmccc.mcdownloader.download.concurrent.CallbackAdapter;

import java.util.concurrent.Callable;

class AnyCombinedTask<T> extends CombinedDownloadTask<T> {

    private CombinedDownloadTask<T>[] tasks;
    private Class<? extends Throwable>[] expectedExceptions;

    public AnyCombinedTask(CombinedDownloadTask<T>[] tasks, Class<? extends Throwable>[] expectedExceptions) {
        this.tasks = tasks;
        this.expectedExceptions = expectedExceptions;
    }

    @Override
    public void execute(CombinedDownloadContext<T> context) throws Exception {
        executeSubtask(context, 0, null);
    }

    public void executeSubtask(final CombinedDownloadContext<T> context, final int index, final Throwable oldEx) throws InterruptedException {
        CombinedDownloadTask<T> task = tasks[index];
        context.submit(task, new CallbackAdapter<T>() {

            @Override
            public void done(T result) {
                context.done(result);
            }

            @Override
            public void failed(final Throwable e) {
                try {
                    context.submit(new Callable<Void>() {

                        @Override
                        public Void call() throws Exception {
                            if (oldEx != null) {
                                e.addSuppressed(oldEx);
                            }
                            int next = index + 1;
                            if (next < tasks.length && canContinue(e)) {
                                executeSubtask(context, next, e);
                            } else {
                                context.failed(e);
                            }
                            return null;
                        }
                    }, null, true);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
            }

            @Override
            public void cancelled() {
                context.cancelled();
            }

        }, false);
    }

    private boolean canContinue(Throwable e) {
        for (Class<? extends Throwable> expected : expectedExceptions) {
            if (expected.isInstance(e)) {
                return true;
            }
        }
        return false;
    }

}
