package org.to2mbn.jmccc.mcdownloader.download.concurrent;

public interface Cancelable {

    /**
     * Cancels the pending task.
     *
     * @param mayInterruptIfRunning if the thread executing this task should be interrupted; otherwise, in-progress
     *                              tasks are allowed to complete
     * @return false if the task cannot be cancelled
     */
    boolean cancel(boolean mayInterruptIfRunning);

}
