package org.to2mbn.jmccc.mcdownloader.provider.quilt;

import org.to2mbn.jmccc.mcdownloader.provider.fabric.FabricDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.fabric.FabricDownloadSource;

public class QuiltDownloadProvider extends FabricDownloadProvider {
    public QuiltDownloadProvider() {
        this(new QuiltDownloadSource());
    }

    public QuiltDownloadProvider(FabricDownloadSource source) {
        super(source);
    }

    @Override
    protected String getLoaderName() {
        return "quilt";
    }
}
