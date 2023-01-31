package org.to2mbn.jmccc.mcdownloader.download.concurrent;

import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;

abstract public class CallbackAdapter<V> implements Callback<V>, DownloadCallback<V>, CombinedDownloadCallback<V> {

    @Override
    public void done(V result) {
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

    @Override
    public <R> DownloadCallback<R> taskStart(DownloadTask<R> task) {
        return null;
    }

}
