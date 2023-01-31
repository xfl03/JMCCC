package org.to2mbn.jmccc.mcdownloader.provider;

public interface ExtendedDownloadProvider extends MinecraftDownloadProvider {

    void setUpstreamProvider(MinecraftDownloadProvider upstreamProvider);

}
