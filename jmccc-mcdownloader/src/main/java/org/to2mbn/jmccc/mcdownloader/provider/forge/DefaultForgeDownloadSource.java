package org.to2mbn.jmccc.mcdownloader.provider.forge;

public class DefaultForgeDownloadSource implements ForgeDownloadSource {

    @Override
    public String getForgeMetadataUrl() {
        return "https://files.minecraftforge.net/net/minecraftforge/forge/maven-metadata.json";
    }

    @Override
    public String getForgePromotionUrl() {
        return "https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json";
    }

    @Override
    public String getForgeMavenRepositoryUrl() {
        return "https://files.minecraftforge.net/maven/";
    }

}
