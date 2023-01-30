package jmccc.example;

import org.to2mbn.jmccc.mcdownloader.MinecraftDownloader;
import org.to2mbn.jmccc.mcdownloader.MinecraftDownloaderBuilder;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.CallbackAdapter;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;
import org.to2mbn.jmccc.version.Version;

public class ExampleDownloader {

    public static void main(String... args) {
        MinecraftDownloader downloader = MinecraftDownloaderBuilder.buildDefault();
        downloader.downloadIncrementally(
                ExampleConfig.MINECRAFT_DIRECTORY, ExampleConfig.MINECRAFT_VERSION, new ExampleDownloadCallback());
    }

    private static class ExampleDownloadCallback extends CallbackAdapter<Version> {
        @Override
        public void done(Version result) {
            System.out.println(result);
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
            System.out.printf("%d/%d\n", done, total);
        }

        @Override
        public void retry(Throwable e, int current, int max) {
            System.out.printf("%d/%d:%s\n", current, max, e);
        }

        @Override
        public <R> DownloadCallback<R> taskStart(DownloadTask<R> task) {
            System.out.println(task.getURI());
            return null;
        }
    }
}
