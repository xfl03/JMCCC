package org.to2mbn.jmccc.mcdownloader.provider.fabric;

public interface FabricDownloadSource {
    default String getFabricMetaBaseUrl() {
        return "https://meta.fabricmc.net/v2/";
    }

    default String getFabricVersionsUrl() {
        return getFabricMetaBaseUrl() + "versions";
    }

    default String getFabricProfileUrl(String minecraftVersion, String loaderVersion) {
        return String.format("%sversions/loader/%s/%s/profile/json", getFabricMetaBaseUrl(),
                minecraftVersion, loaderVersion);
    }

    class Default implements FabricDownloadSource {
    }
}
