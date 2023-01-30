package jmccc.cli.download;

import org.to2mbn.jmccc.mcdownloader.provider.DefaultLayoutProvider;
import org.to2mbn.jmccc.mcdownloader.provider.forge.ForgeDownloadSource;

public class BmclApiProvider extends DefaultLayoutProvider implements ForgeDownloadSource {

    @Override
    protected String getLibraryBaseURL() {
        return "https://bmclapi2.bangbang93.com/libraries/";
    }

    @Override
    protected String getVersionListURL() {
        return "https://bmclapi2.bangbang93.com/mc/game/version_manifest_v2.json";
    }

    @Override
    protected String getAssetBaseURL() {
        return "https://bmclapi2.bangbang93.com/assets/";
    }

    @Override
    public String getForgeVersionListUrl() {
        return "https://bmclapi2.bangbang93.com/maven/net/minecraftforge/forge/json";
    }

    @Override
    public String getForgeMavenRepositoryUrl() {
        return "https://bmclapi2.bangbang93.com/maven/";
    }
}
