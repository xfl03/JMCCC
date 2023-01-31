package jmccc.cli.download;

import org.to2mbn.jmccc.mcdownloader.provider.fabric.FabricVersion;
import org.to2mbn.jmccc.mcdownloader.provider.forge.ForgeVersion;
import org.to2mbn.jmccc.mcdownloader.provider.liteloader.LiteloaderVersion;

import java.util.concurrent.ExecutionException;

public class ModLoaderDownloader {
    public static ForgeVersion getLatestForge() throws ExecutionException, InterruptedException {
        return CliDownloader.get(CliDownloader.forgeProvider.forgeVersionList()).getLatest();
    }

    public static LiteloaderVersion getLatestLiteLoader() throws ExecutionException, InterruptedException {
        return CliDownloader.get(CliDownloader.liteloaderProvider.liteloaderVersionList())
                .getSnapshot("1.12.2");
    }

    public static FabricVersion getLatestFabric() throws ExecutionException, InterruptedException {
        return CliDownloader.get(CliDownloader.fabricProvider.fabricVersionList()).getLatestRelease();
    }

    public static FabricVersion getLatestFabricSnapshot() throws ExecutionException, InterruptedException {
        return CliDownloader.get(CliDownloader.fabricProvider.fabricVersionList()).getLatestSnapshot();
    }

    public static FabricVersion getLatestQuilt() throws ExecutionException, InterruptedException {
        return CliDownloader.get(CliDownloader.quiltProvider.fabricVersionList()).getLatestRelease();
    }

    public static FabricVersion getLatestQuiltSnapshot() throws ExecutionException, InterruptedException {
        return CliDownloader.get(CliDownloader.quiltProvider.fabricVersionList()).getLatestSnapshot();
    }
}
