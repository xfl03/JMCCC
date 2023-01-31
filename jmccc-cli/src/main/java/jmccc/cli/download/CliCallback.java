package jmccc.cli.download;

import org.to2mbn.jmccc.mcdownloader.download.concurrent.CallbackAdapter;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;

public class CliCallback<T> extends CallbackAdapter<T> {
    @Override
    public void done(T result) {
        String res = result.toString();
        System.out.println("Done: " + res.substring(0, Math.min(res.length(), 233)));
    }

    @Override
    public void failed(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void cancelled() {
        System.out.println("Cancelled");
    }

    @Override
    public void updateProgress(long done, long total) {
        if (Math.random() < 0.33) {
            System.out.printf("Progress: %d/%d\n", done, total);
        }
    }

    @Override
    public void retry(Throwable e, int current, int max) {
        System.out.printf("Retry: %d/%d:%s\n", current, max, e);
    }

    @Override
    public <R> DownloadCallback<R> taskStart(DownloadTask<R> task) {
        System.out.println("Start: " + task.getURI());
        return new CliCallback<R>();
    }
}