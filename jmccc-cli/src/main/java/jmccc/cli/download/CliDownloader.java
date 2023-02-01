package jmccc.cli.download;

import org.to2mbn.jmccc.mcdownloader.MinecraftDownloader;
import org.to2mbn.jmccc.mcdownloader.MinecraftDownloaderBuilder;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.mcdownloader.provider.DownloadProviderChain;
import org.to2mbn.jmccc.mcdownloader.provider.MojangDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.fabric.FabricDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.forge.ForgeDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.liteloader.LiteloaderDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.quilt.QuiltDownloadProvider;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Version;

import java.util.concurrent.ExecutionException;

public class CliDownloader {
    public static ForgeDownloadProvider forgeProvider;
    public static LiteloaderDownloadProvider liteloaderProvider;
    public static FabricDownloadProvider fabricProvider;
    public static FabricDownloadProvider quiltProvider;
    public static MinecraftDownloader downloader;

    public static void init(boolean isBmclApi) {
        BmclApiProvider bmclApiProvider = isBmclApi ? new BmclApiProvider() : null;
        forgeProvider = new ForgeDownloadProvider(bmclApiProvider);
        liteloaderProvider = new LiteloaderDownloadProvider(bmclApiProvider);
        fabricProvider = new FabricDownloadProvider();
        quiltProvider = new QuiltDownloadProvider();
        downloader = MinecraftDownloaderBuilder.create()
                .providerChain(DownloadProviderChain.create()
                        .baseProvider(bmclApiProvider == null ? new MojangDownloadProvider() : bmclApiProvider)
                        .addProvider(forgeProvider)
                        .addProvider(liteloaderProvider)
                        .addProvider(fabricProvider)
                        .addProvider(quiltProvider)
                )
                .build();
    }

    public static String getLatestRelease() throws ExecutionException, InterruptedException {
        return downloader.fetchRemoteVersionList(null).get().getLatestRelease();
    }

    public static String getLatestSnapshot() throws ExecutionException, InterruptedException {
        return downloader.fetchRemoteVersionList(null).get().getLatestSnapshot();
    }

    public static Version download(MinecraftDirectory dir, String version) throws ExecutionException, InterruptedException {
        return downloader.downloadIncrementally(dir, version, new CliCallback<>()).get();
    }

    public static void downloadLibrary(MinecraftDirectory dir, Library lib) throws ExecutionException, InterruptedException {
        downloader.download(downloader.getProvider().library(dir, lib), new CliCallback<>()).get();
    }

    public static <T> T get(CombinedDownloadTask<T> task) throws ExecutionException, InterruptedException {
        return CliDownloader.downloader.download(task, new CliCallback<>()).get();
    }
}
