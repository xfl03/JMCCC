package jmccc.cli.download;

import org.to2mbn.jmccc.mcdownloader.MinecraftDownloader;
import org.to2mbn.jmccc.mcdownloader.MinecraftDownloaderBuilder;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.mcdownloader.provider.DownloadProviderChain;
import org.to2mbn.jmccc.mcdownloader.provider.fabric.FabricDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.forge.ForgeDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.liteloader.LiteloaderDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.quilt.QuiltDownloadProvider;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Version;

import java.util.concurrent.ExecutionException;

public class SimpleDownloader {
    public static BmclApiProvider bmclApiProvider = new BmclApiProvider();
    public static ForgeDownloadProvider forgeProvider = new ForgeDownloadProvider(bmclApiProvider);
    public static LiteloaderDownloadProvider liteloaderProvider = new LiteloaderDownloadProvider(bmclApiProvider);
    public static FabricDownloadProvider fabricProvider = new FabricDownloadProvider();
    public static FabricDownloadProvider quiltProvider = new QuiltDownloadProvider();

    public static MinecraftDownloader downloader = MinecraftDownloaderBuilder.create()
            .providerChain(DownloadProviderChain.create()
                    .baseProvider(bmclApiProvider)
                    .addProvider(forgeProvider)
                    .addProvider(liteloaderProvider)
                    .addProvider(fabricProvider)
                    .addProvider(quiltProvider)
            )
            .build();

    public static String getLatestRelease() throws ExecutionException, InterruptedException {
        return downloader.fetchRemoteVersionList(null).get().getLatestRelease();
    }

    public static String getLatestSnapshot() throws ExecutionException, InterruptedException {
        return downloader.fetchRemoteVersionList(null).get().getLatestSnapshot();
    }

    public static Version download(MinecraftDirectory dir, String version) throws ExecutionException, InterruptedException {
        return downloader.downloadIncrementally(dir, version, new SimpleCallback<>()).get();
    }

    public static void downloadLibrary(MinecraftDirectory dir, Library lib) throws ExecutionException, InterruptedException {
        downloader.download(downloader.getProvider().library(dir, lib), new SimpleCallback<>()).get();
    }

    public static <T> T get(CombinedDownloadTask<T> task) throws ExecutionException, InterruptedException {
        return SimpleDownloader.downloader.download(task, new SimpleCallback<>()).get();
    }
}
