package org.to2mbn.jmccc.mcdownloader.download.concurrent;

public interface DownloadCallback<T> extends Callback<T> {

    /**
     * Calls when the progress of the download operation updated.
     *
     * @param done  the bytes downloaded
     * @param total the total bytes, -1 if unknown
     */
    void updateProgress(long done, long total);

    /**
     * Calls when download failed and the downloader will retry the download task.
     * <p>
     * Notes: {@link #failed(Throwable)} will be called only when the download failed and the downloader won't retry
     * it any more. If the downloader will retry the download task, this method will be called, instead of
     * {@link #failed(Throwable)}.
     *
     * @param e       the cause of download failure
     * @param current the retry count (1 for the first, max-1 for the latest)
     * @param max     the max number of tries
     */
    void retry(Throwable e, int current, int max);

}
