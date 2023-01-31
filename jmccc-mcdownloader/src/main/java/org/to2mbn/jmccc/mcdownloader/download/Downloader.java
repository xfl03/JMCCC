package org.to2mbn.jmccc.mcdownloader.download;

import org.to2mbn.jmccc.mcdownloader.download.concurrent.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.Shutdownable;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;

import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

public interface Downloader extends Shutdownable {

    /**
     * Submits a download task asynchronously.
     *
     * @param task     the download task
     * @param callback download callback
     * @param <T>      the result of the task
     * @return future representing pending completion of the download
     * @throws NullPointerException       <code>task == null</code>
     * @throws RejectedExecutionException if the downloader has been shutdown
     */
    <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback);

    /**
     * Submits a download task asynchronously.
     * <p>
     * The task will be retried only when an <code>IOException</code> occurs.
     * <br>
     * <code>tries</code> is the max number of tries. For example: If
     * <code>tries==1</code>, the download won't be retried (try 1 time, retry 0
     * time); If <code>tries==5</code>, the download will be retried at most 4
     * times.
     *
     * @param task     the download task
     * @param callback download callback
     * @param tries    the max number of tries
     * @param <T>      the result of the task
     * @return future representing pending completion of the download
     * @throws NullPointerException       <code>task == null</code>
     * @throws IllegalArgumentException   if <code>tries &lt; 1</code>
     * @throws RejectedExecutionException if the downloader has been shutdown
     */
    <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback, int tries);

}
