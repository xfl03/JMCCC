package org.to2mbn.jmccc.mcdownloader.provider.fabric;

import org.to2mbn.jmccc.mcdownloader.download.cache.CacheNames;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.MemoryDownloadTask;
import org.to2mbn.jmccc.mcdownloader.provider.*;
import org.to2mbn.jmccc.option.MinecraftDirectory;

public class FabricDownloadProvider extends AbstractMinecraftDownloadProvider implements ExtendedDownloadProvider {
    private MinecraftDownloadProvider upstreamProvider;
    private FabricDownloadSource source;

    public FabricDownloadProvider(FabricDownloadSource source) {
        this.source = source;
    }

    public FabricDownloadProvider() {
        this(new FabricDownloadSource.Default());
    }

    @Override
    public void setUpstreamProvider(MinecraftDownloadProvider upstreamProvider) {
        this.upstreamProvider = upstreamProvider;
    }

    public CombinedDownloadTask<FabricVersionList> fabricVersionList() {
        return CombinedDownloadTask.single(new MemoryDownloadTask(source.getFabricVersionsUrl())
                .andThen(new JsonDecoder())
                .andThen(FabricVersionList::fromJson)
                .cacheable()
                .cachePool(CacheNames.FABRIC_VERSION_LIST));
    }

    @Override
    public CombinedDownloadTask<String> gameVersionJson(MinecraftDirectory mcdir, String version) {
        FabricVersion fabricVersion = FabricVersion.resolve(getLoaderName(), version);
        if (fabricVersion == null) {
            return null;
        }
        String url = source.getFabricProfileUrl(
                fabricVersion.getMinecraftVersion(), fabricVersion.getFabricLoaderVersion());
        return CombinedDownloadTask.single(new MemoryDownloadTask(url))
                .andThen(new JsonDecoder())
                .andThen(new VersionJsonInstaller(mcdir));
    }

    protected String getLoaderName() {
        return "fabric";
    }
}
