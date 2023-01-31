package org.to2mbn.jmccc.mcdownloader.provider.quilt;

import org.to2mbn.jmccc.mcdownloader.provider.fabric.FabricDownloadSource;

public class QuiltDownloadSource implements FabricDownloadSource {
    public String getFabricMetaBaseUrl() {
        return "https://meta.quiltmc.org/v3/";
    }
}
