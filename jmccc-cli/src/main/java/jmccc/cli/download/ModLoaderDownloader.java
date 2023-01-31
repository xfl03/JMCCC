package jmccc.cli.download;

import org.to2mbn.jmccc.mcdownloader.provider.forge.ForgeVersion;

import java.util.concurrent.ExecutionException;

public class ModLoaderDownloader {
    public static ForgeVersion getLatestForge(String minecraftVersion) throws ExecutionException, InterruptedException {
        return SimpleDownloader.downloader.download(SimpleDownloader.forgeProvider.forgeVersionList(), null)
                .get().getLatest(minecraftVersion);
    }
}
