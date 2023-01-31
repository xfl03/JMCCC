package org.to2mbn.jmccc.mcdownloader.provider.fabric;

import org.to2mbn.jmccc.internal.org.json.JSONArray;
import org.to2mbn.jmccc.internal.org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FabricVersionList {
    private final List<String> minecraftReleaseVersions;
    private final List<String> minecraftSnapshotVersions;
    private final List<String> fabricLoaderVersions;
    private final String loaderName;

    public FabricVersionList(List<String> minecraftReleaseVersions, List<String> minecraftSnapshotVersions, List<String> fabricLoaderVersions, String loaderName) {
        this.minecraftReleaseVersions = minecraftReleaseVersions;
        this.minecraftSnapshotVersions = minecraftSnapshotVersions;
        this.fabricLoaderVersions = fabricLoaderVersions;
        this.loaderName = loaderName;
    }

    public FabricVersion getLatest(String minecraftVersion) {
        return new FabricVersion(minecraftVersion, fabricLoaderVersions.get(0), loaderName);
    }

    public FabricVersion getLatestRelease() {
        return new FabricVersion(minecraftReleaseVersions.get(0), fabricLoaderVersions.get(0), loaderName);
    }

    public FabricVersion getLatestSnapshot() {
        return new FabricVersion(minecraftSnapshotVersions.get(0), fabricLoaderVersions.get(0), loaderName);
    }

    public static FabricVersionList fromJson(JSONObject json) {
        JSONArray game = json.getJSONArray("game");
        List<String> minecraftReleaseVersions = new ArrayList<>();
        List<String> minecraftSnapshotVersions = new ArrayList<>();
        for (Object obj : game) {
            JSONObject it = (JSONObject) obj;
            (it.getBoolean("stable") ? minecraftReleaseVersions : minecraftSnapshotVersions)
                    .add(it.getString("version"));
        }

        JSONArray loader = json.getJSONArray("loader");
        List<String> fabricLoaderVersions = new ArrayList<>();
        String loaderName = "fabric";
        for (Object obj : loader) {
            JSONObject it = (JSONObject) obj;
            if (it.getString("maven").contains("quilt")) {
                loaderName = "quilt";
            }
            fabricLoaderVersions.add(it.getString("version"));
        }

        return new FabricVersionList(minecraftReleaseVersions, minecraftSnapshotVersions,
                fabricLoaderVersions, loaderName);
    }

    @Override
    public String toString() {
        return "FabricVersionList{ Latest: " + fabricLoaderVersions.get(0) + " }";
    }
}
