package jmccc.cli.download;

import org.to2mbn.jmccc.mcdownloader.provider.forge.ForgeVersion;
import org.to2mbn.jmccc.mcdownloader.provider.liteloader.LiteloaderVersion;

import java.util.concurrent.ExecutionException;

public class ModLoaderDownloader {
    public static ForgeVersion getLatestForge() throws ExecutionException, InterruptedException {
        return SimpleDownloader.downloader.download(SimpleDownloader.forgeProvider.forgeVersionList(), null)
                .get().getLatest();
    }

    public static LiteloaderVersion getLatestLiteLoader() throws ExecutionException, InterruptedException {
        return SimpleDownloader.downloader.download(SimpleDownloader.liteloaderProvider.liteloaderVersionList(), null)
                .get().getSnapshot("1.12.2");
    }
}
