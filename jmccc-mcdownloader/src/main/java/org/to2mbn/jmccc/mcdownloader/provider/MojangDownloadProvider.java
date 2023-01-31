package org.to2mbn.jmccc.mcdownloader.provider;

public class MojangDownloadProvider extends DefaultLayoutProvider {

    @Override
    protected String getLibraryBaseURL() {
        return "https://libraries.minecraft.net/";
    }

    @Override
    protected String getVersionListURL() {
        return "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    }

    @Override
    protected String getAssetBaseURL() {
        return "https://resources.download.minecraft.net/";
    }

}
