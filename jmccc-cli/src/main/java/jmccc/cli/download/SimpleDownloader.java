package jmccc.cli.download;

import jmccc.cli.Config;
import org.to2mbn.jmccc.mcdownloader.MinecraftDownloader;
import org.to2mbn.jmccc.mcdownloader.MinecraftDownloaderBuilder;
import org.to2mbn.jmccc.mcdownloader.provider.DownloadProviderChain;
import org.to2mbn.jmccc.mcdownloader.provider.forge.ForgeDownloadProvider;
import org.to2mbn.jmccc.version.Version;

import java.util.concurrent.ExecutionException;

public class SimpleDownloader {
    public static BmclApiProvider bmclApiProvider = new BmclApiProvider();
    public static ForgeDownloadProvider forgeProvider = new ForgeDownloadProvider(bmclApiProvider);

    public static MinecraftDownloader downloader = MinecraftDownloaderBuilder.create()
            .providerChain(DownloadProviderChain.create()
                    .baseProvider(bmclApiProvider)
                    .addProvider(forgeProvider)
            )
            .build();

    public static String getLatestRelease() throws ExecutionException, InterruptedException {
        return downloader.fetchRemoteVersionList(null).get().getLatestRelease();
    }

    public static Version download(String version) throws ExecutionException, InterruptedException {
        return downloader.downloadIncrementally(Config.MINECRAFT_DIRECTORY, version, new SimpleCallback<>()).get();
    }
}
