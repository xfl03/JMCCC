package org.to2mbn.jmccc.mcdownloader.download.combine;

import org.to2mbn.jmccc.mcdownloader.download.concurrent.Callback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.CombinedDownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface CombinedDownloadContext<T> extends Callback<T> {

    <R> Future<R> submit(Callable<R> task, Callback<R> callback, boolean fatal) throws InterruptedException;

    <R> Future<R> submit(DownloadTask<R> task, DownloadCallback<R> callback, boolean fatal) throws InterruptedException;

    <R> Future<R> submit(CombinedDownloadTask<R> task, CombinedDownloadCallback<R> callback, boolean fatal) throws InterruptedException;

    void awaitAllTasks(Callable<Void> callback) throws InterruptedException;

}
