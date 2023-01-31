package org.to2mbn.jmccc.mcdownloader.provider.liteloader;

import org.to2mbn.jmccc.internal.org.json.JSONObject;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.MemoryDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.ResultProcessor;
import org.to2mbn.jmccc.mcdownloader.provider.JsonDecoder;

public class DefaultLiteloaderDownloadSource implements LiteloaderDownloadSource {

    @Override
    public String getLiteloaderManifestUrl() {
        return "https://dl.liteloader.com/versions/versions.json";
    }

    @Override
    public CombinedDownloadTask<JSONObject> liteloaderSnapshotVersionJson(LiteloaderVersion liteloader) {
        return CombinedDownloadTask.single(
                new MemoryDownloadTask("https://raw.githubusercontent.com/Mumfrey/LiteLoaderInstaller/" + liteloader.getMinecraftVersion() + "/src/main/resources/install_profile.json")
                        .andThen(new JsonDecoder())
                        .andThen(new ResultProcessor<JSONObject, JSONObject>() {

                            @Override
                            public JSONObject process(JSONObject installProfile) throws Exception {
                                return installProfile.getJSONObject("versionInfo");
                            }
                        })
                        .cacheable());
    }

}
