package jmccc.cli.download;

import org.to2mbn.jmccc.internal.org.json.JSONObject;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.MemoryDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.ResultProcessor;
import org.to2mbn.jmccc.mcdownloader.provider.DefaultLayoutProvider;
import org.to2mbn.jmccc.mcdownloader.provider.JsonDecoder;
import org.to2mbn.jmccc.mcdownloader.provider.forge.ForgeDownloadSource;
import org.to2mbn.jmccc.mcdownloader.provider.liteloader.LiteloaderDownloadSource;
import org.to2mbn.jmccc.mcdownloader.provider.liteloader.LiteloaderVersion;

public class BmclApiProvider extends DefaultLayoutProvider implements ForgeDownloadSource, LiteloaderDownloadSource {

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

    @Override
    public String getLiteloaderManifestUrl() {
        return "https://bmclapi.bangbang93.com/maven/com/mumfrey/liteloader/versions.json";
    }

    @Override
    public CombinedDownloadTask<JSONObject> liteloaderSnapshotVersionJson(LiteloaderVersion liteloader) {
        return CombinedDownloadTask.single(
                new MemoryDownloadTask("https://ghproxy.com/https://raw.githubusercontent.com/Mumfrey/LiteLoaderInstaller/" + liteloader.getMinecraftVersion() + "/src/main/resources/install_profile.json")
                        .andThen(new JsonDecoder())
                        .andThen(installProfile -> installProfile.getJSONObject("versionInfo"))
                        .cacheable());
    }
}
