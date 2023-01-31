package org.to2mbn.jmccc.mcdownloader.provider;

import org.to2mbn.jmccc.mcdownloader.RemoteVersionList;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Version;

import java.util.Set;

class DownloadProviderTree implements MinecraftDownloadProvider {

    private MinecraftDownloadProvider left;
    private MinecraftDownloadProvider right;

    public DownloadProviderTree(MinecraftDownloadProvider left, MinecraftDownloadProvider right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public CombinedDownloadTask<RemoteVersionList> versionList() {
        CombinedDownloadTask<RemoteVersionList> result = left.versionList();
        if (result == null && right != null) {
            result = right.versionList();
        }
        checkFinalResult(result);
        return result;
    }

    @Override
    public CombinedDownloadTask<Set<Asset>> assetsIndex(MinecraftDirectory mcdir, Version version) {
        CombinedDownloadTask<Set<Asset>> result = left.assetsIndex(mcdir, version);
        if (result == null && right != null) {
            result = right.assetsIndex(mcdir, version);
        }
        checkFinalResult(result);
        return result;
    }

    @Override
    public CombinedDownloadTask<Void> gameJar(MinecraftDirectory mcdir, Version version) {
        CombinedDownloadTask<Void> result = left.gameJar(mcdir, version);
        if (result == null && right != null) {
            result = right.gameJar(mcdir, version);
        }
        checkFinalResult(result);
        return result;
    }

    @Override
    public CombinedDownloadTask<String> gameVersionJson(MinecraftDirectory mcdir, String version) {
        CombinedDownloadTask<String> result = left.gameVersionJson(mcdir, version);
        if (result == null && right != null) {
            result = right.gameVersionJson(mcdir, version);
        }
        checkFinalResult(result);
        return result;
    }

    @Override
    public CombinedDownloadTask<Void> library(MinecraftDirectory mcdir, Library library) {
        CombinedDownloadTask<Void> result = left.library(mcdir, library);
        if (result == null && right != null) {
            result = right.library(mcdir, library);
        }
        checkFinalResult(result);
        return result;
    }

    @Override
    public CombinedDownloadTask<Void> asset(MinecraftDirectory mcdir, Asset asset) {
        CombinedDownloadTask<Void> result = left.asset(mcdir, asset);
        if (result == null && right != null) {
            result = right.asset(mcdir, asset);
        }
        checkFinalResult(result);
        return result;
    }

    private void checkFinalResult(Object result) {
        if (result == null) {
            throw new IllegalArgumentException("No provider is available for this operation");
        }
    }

}
