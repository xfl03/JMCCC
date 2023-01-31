package org.to2mbn.jmccc.mcdownloader.provider;

import org.to2mbn.jmccc.mcdownloader.RemoteVersionList;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Version;

import java.util.Set;

abstract public class AbstractMinecraftDownloadProvider implements MinecraftDownloadProvider {

    // @formatter:off
    @Override
    public CombinedDownloadTask<RemoteVersionList> versionList() {
        return null;
    }

    @Override
    public CombinedDownloadTask<Set<Asset>> assetsIndex(MinecraftDirectory mcdir, Version version) {
        return null;
    }

    @Override
    public CombinedDownloadTask<Void> gameJar(MinecraftDirectory mcdir, Version version) {
        return null;
    }

    @Override
    public CombinedDownloadTask<String> gameVersionJson(MinecraftDirectory mcdir, String version) {
        return null;
    }

    @Override
    public CombinedDownloadTask<Void> library(MinecraftDirectory mcdir, Library library) {
        return null;
    }

    @Override
    public CombinedDownloadTask<Void> asset(MinecraftDirectory mcdir, Asset asset) {
        return null;
    }
    // @formatter:on

}
