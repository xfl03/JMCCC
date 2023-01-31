package org.to2mbn.jmccc.mcdownloader;

import org.to2mbn.jmccc.internal.org.json.JSONArray;
import org.to2mbn.jmccc.internal.org.json.JSONException;
import org.to2mbn.jmccc.internal.org.json.JSONObject;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class RemoteVersionList implements Serializable {

    private static final long serialVersionUID = 1L;
    private String latestSnapshot;
    private String latestRelease;
    private Map<String, RemoteVersion> versions;
    /**
     * Constructor of RemoteVersionList.
     *
     * @param latestSnapshot the version of the latest snapshot
     * @param latestRelease  the version of the latest release
     * @param versions       the versions
     * @throws NullPointerException <code>versions==null</code>
     */
    public RemoteVersionList(String latestSnapshot, String latestRelease, Map<String, RemoteVersion> versions) {
        this.latestSnapshot = latestSnapshot;
        this.latestRelease = latestRelease;
        this.versions = Objects.requireNonNull(versions);
    }

    public static RemoteVersionList fromJson(JSONObject json) throws JSONException {
        String latestSnapshot = null;
        String latestRelease = null;
        if (json.has("latest")) {
            JSONObject latest = json.getJSONObject("latest");
            latestSnapshot = latest.optString("snapshot", null);
            latestRelease = latest.optString("release", null);
        }

        JSONArray jsonVersions = json.getJSONArray("versions");
        Map<String, RemoteVersion> versions = new LinkedHashMap<>();
        for (int i = 0; i < jsonVersions.length(); i++) {
            RemoteVersion version = RemoteVersion.fromJson(jsonVersions.getJSONObject(i));
            versions.put(version.getVersion(), version);
        }
        return new RemoteVersionList(latestSnapshot, latestRelease, Collections.unmodifiableMap(versions));
    }

    /**
     * Gets the version of the latest snapshot.
     *
     * @return the version of the latest snapshot
     */
    public String getLatestSnapshot() {
        return latestSnapshot;
    }

    /**
     * Gets the version of the latest release.
     *
     * @return the version of the latest release
     */
    public String getLatestRelease() {
        return latestRelease;
    }

    /**
     * Gets all the versions.
     *
     * @return all the versions
     */
    public Map<String, RemoteVersion> getVersions() {
        return versions;
    }

    @Override
    public String toString() {
        return "[latestSnapshot=" + latestSnapshot + ", latestRelease=" + latestRelease + ", versions=" + versions + "]";
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
        if (obj instanceof RemoteVersionList) {
            RemoteVersionList another = (RemoteVersionList) obj;
            return Objects.equals(versions, another.versions)
                    && Objects.equals(latestRelease, another.latestRelease)
                    && Objects.equals(latestSnapshot, another.latestSnapshot);
        }
        return false;
    }
}
