package org.to2mbn.jmccc.mcdownloader.provider.liteloader;

import org.to2mbn.jmccc.internal.org.json.JSONArray;
import org.to2mbn.jmccc.internal.org.json.JSONObject;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import static org.to2mbn.jmccc.mcdownloader.provider.liteloader.LiteloaderDownloadProvider.LITELOADER_ARTIFACT_ID;
import static org.to2mbn.jmccc.mcdownloader.provider.liteloader.LiteloaderDownloadProvider.LITELOADER_GROUP_ID;

public class LiteloaderVersionList implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * The outside map's key is the minecraft version, value is the artifacts.
     * The inside map's key is the artifact name, value is artifact.
     */
    private Map<String, Map<String, LiteloaderVersion>> versions;
    private Map<String, LiteloaderVersion> latests;
    private Map<String, LiteloaderVersion> snapshots;
    public LiteloaderVersionList(Map<String, Map<String, LiteloaderVersion>> versions) {
        Objects.requireNonNull(versions);
        this.versions = versions;

        this.latests = Collections.unmodifiableMap(filterByArtefactId(versions, "latest"));
        this.snapshots = Collections.unmodifiableMap(filterByArtefactId(versions, "snapshot"));
    }

    public static LiteloaderVersionList fromJson(JSONObject json) {
        Map<String, Map<String, LiteloaderVersion>> versions = new TreeMap<>();
        JSONObject versionsJson = json.getJSONObject("versions");
        for (String mcversion : versionsJson.keySet()) {
            Map<String, LiteloaderVersion> artefacts = new TreeMap<>();
            JSONObject versionRootJson = versionsJson.getJSONObject(mcversion);

            String repoUrl = null;
            JSONObject repoJson = versionRootJson.optJSONObject("repo");
            if (repoJson != null) {
                repoUrl = repoJson.optString("url", null);
            }

            JSONObject artefactsJson = versionRootJson.optJSONObject("artefacts");
            if (artefactsJson != null) {
                JSONObject liteloaderArtefactsJson = artefactsJson.getJSONObject(LITELOADER_GROUP_ID + ":" + LITELOADER_ARTIFACT_ID);
                for (String artefactId : liteloaderArtefactsJson.keySet()) {
                    JSONObject artefactJson = liteloaderArtefactsJson.getJSONObject(artefactId);
                    String liteloaderVersion = artefactJson.getString("version");
                    String tweakClass = artefactJson.optString("tweakClass", null);
                    Set<JSONObject> libraries = parseLibraries(artefactJson.optJSONArray("libraries"));
                    artefacts.put(artefactId, new LiteloaderVersion(mcversion, liteloaderVersion, tweakClass, null, Collections.unmodifiableSet(libraries)));
                }
            }

            JSONObject snapshotsJson = versionRootJson.optJSONObject("snapshots");
            if (snapshotsJson != null) {
                Set<JSONObject> libraries = parseLibraries(snapshotsJson.optJSONArray("libraries"));
                artefacts.put("snapshot", new LiteloaderVersion(mcversion, mcversion + "-SNAPSHOT", null, repoUrl, Collections.unmodifiableSet(libraries)));
            }

            if (!artefacts.isEmpty()) {
                versions.put(mcversion, artefacts);
            }
        }
        return new LiteloaderVersionList(versions);
    }

    private static Set<JSONObject> parseLibraries(JSONArray librariesJson) {
        Set<JSONObject> libraries = null;
        if (librariesJson != null) {
            libraries = new LinkedHashSet<>();
            for (int i = 0; i < librariesJson.length(); i++)
                libraries.add(librariesJson.getJSONObject(i));
        }
        return libraries;
    }

    private static Map<String, LiteloaderVersion> filterByArtefactId(Map<String, Map<String, LiteloaderVersion>> versions, String id) {
        Map<String, LiteloaderVersion> result = new TreeMap<>();
        for (Entry<String, Map<String, LiteloaderVersion>> entry : versions.entrySet()) {
            LiteloaderVersion latest = entry.getValue().get(id);
            if (latest != null) {
                result.put(entry.getKey(), latest);
            }
        }
        return result;
    }

    public Map<String, Map<String, LiteloaderVersion>> getAllArtefacts() {
        return versions;
    }

    public Map<String, LiteloaderVersion> getArtefacts(String minecraftVersion) {
        return versions.get(minecraftVersion);
    }

    public LiteloaderVersion getArtefact(String minecraftVersion, String artefactId) {
        Map<String, LiteloaderVersion> artefacts = versions.get(minecraftVersion);
        if (artefacts != null) {
            return artefacts.get(artefactId);
        }
        return null;
    }

    /**
     * Gets the latest liteloader of the given minecraft version.
     *
     * @param minecraftVersion the minecraft version
     * @return the liteloader version, null if there's no such a liteloader
     * version
     */
    public LiteloaderVersion getLatest(String minecraftVersion) {
        return getArtefact(minecraftVersion, "latest");
    }

    /**
     * Gets the snapshot liteloader of the given minecraft version.
     *
     * @param minecraftVersion the minecraft version
     * @return the liteloader version, null if there's no such a liteloader
     * version
     */
    public LiteloaderVersion getSnapshot(String minecraftVersion) {
        return getArtefact(minecraftVersion, "snapshot");
    }

    /**
     * Gets all the latest liteloaders.
     * <p>
     * The key is the minecraft version, the value is the liteloader version.
     *
     * @return all the latest liteloaders
     */
    public Map<String, LiteloaderVersion> getLatests() {
        return latests;
    }

    /**
     * Gets all the snapshot liteloaders.
     * <p>
     * The key is the minecraft version, the value is the liteloader version.
     *
     * @return all the snapshot liteloaders
     */
    public Map<String, LiteloaderVersion> getSnapshots() {
        return snapshots;
    }

    @Override
    public String toString() {
        return "LiteloaderVersionList [" + versions + "]";
    }

    @Override
    public int hashCode() {
        return versions.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof LiteloaderVersionList) {
            LiteloaderVersionList another = (LiteloaderVersionList) obj;
            return Objects.equals(versions, another.versions)
                    && Objects.equals(latests, another.latests)
                    && Objects.equals(snapshots, another.snapshots);
        }
        return false;
    }

}
