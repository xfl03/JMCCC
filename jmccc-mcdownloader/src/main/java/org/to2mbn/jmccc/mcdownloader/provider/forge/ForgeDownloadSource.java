package org.to2mbn.jmccc.mcdownloader.provider.forge;

public interface ForgeDownloadSource {

    String getForgeMetadataUrl();
    String getForgePromotionUrl();

    String getForgeMavenRepositoryUrl();

}
