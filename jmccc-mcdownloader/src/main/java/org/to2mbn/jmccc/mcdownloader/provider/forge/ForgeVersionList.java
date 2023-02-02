package org.to2mbn.jmccc.mcdownloader.provider.forge;

import org.to2mbn.jmccc.internal.org.json.JSONArray;
import org.to2mbn.jmccc.internal.org.json.JSONObject;

import java.io.Serializable;
import java.util.*;

public class ForgeVersionList implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Map<String, List<ForgeVersion>> versions;
    private final Map<String, ForgeVersion> latests;
    private final Map<String, ForgeVersion> recommendeds;
    private final Map<String, ForgeVersion> forgeVersionMapping;
    private final ForgeVersion latest;
    private final ForgeVersion recommended;

    public ForgeVersionList(
            Map<String, List<ForgeVersion>> versions, Map<String, ForgeVersion> latests,
            Map<String, ForgeVersion> recommendeds, Map<String, ForgeVersion> forgeVersionMapping, ForgeVersion latest,
            ForgeVersion recommended) {
        Objects.requireNonNull(versions);
        Objects.requireNonNull(latests);
        Objects.requireNonNull(recommendeds);
        Objects.requireNonNull(forgeVersionMapping);
        this.versions = versions;
        this.latests = latests;
        this.recommendeds = recommendeds;
        this.forgeVersionMapping = forgeVersionMapping;
        this.latest = latest;
        this.recommended = recommended;
    }

    public static ForgeVersionList fromJson(JSONObject metaJson, JSONObject promoJson) {
        Map<String, List<ForgeVersion>> versions = new TreeMap<>();
        Map<String, ForgeVersion> latests = new TreeMap<>();
        Map<String, ForgeVersion> recommendeds = new TreeMap<>();
        Map<String, ForgeVersion> forgeVersionMapping = new TreeMap<>();
        ForgeVersion latest = null;
        ForgeVersion recommended = null;

        for (String mcVersion : metaJson.keySet()) {
            JSONArray versionJson = metaJson.getJSONArray(mcVersion);
            for (Object forgeVersionObj : versionJson) {
                ForgeVersion version = ForgeVersion.from((String) forgeVersionObj);
                versions.computeIfAbsent(mcVersion, it -> new ArrayList<>()).add(version);
                forgeVersionMapping.put(version.getForgeVersion(), version);
            }

        }
        JSONObject promos = promoJson.getJSONObject("promos");
        for (String key : promos.keySet()) {
            ForgeVersion version = forgeVersionMapping.get(promos.getString(key));
            if (key.endsWith("-latest")) {
                // 7 - length of "-latest"
                latests.put(key.substring(0, key.length() - 7), version);
                latest = version;
            } else if (key.endsWith("-recommended")) {
                // 12 - length of "-recommended"
                recommendeds.put(key.substring(0, key.length() - 12), version);
                recommended = version;
            }
        }
        return new ForgeVersionList(Collections.unmodifiableMap(versions),
                Collections.unmodifiableMap(latests),
                Collections.unmodifiableMap(recommendeds),
                Collections.unmodifiableMap(forgeVersionMapping),
                latest,
                recommended);
    }

    /**
     * Gets all the forge versions of given minecraft version.
     *
     * @return all the forge versions
     */
    public List<ForgeVersion> getVersions(String mcversion) {
        return versions.get(mcversion);
    }

    /**
     * Gets all the latest versions.
     *
     * @return a map including all the latest versions, key is the minecraft
     * version, value is the latest forge version of the minecraft
     * version
     */
    public Map<String, ForgeVersion> getLatests() {
        return latests;
    }

    /**
     * Gets all the recommended versions.
     *
     * @return a map including all the recommended versions, key is the
     * minecraft version, value is the recommended forge version of the
     * minecraft version
     */
    public Map<String, ForgeVersion> getRecommendeds() {
        return recommendeds;
    }

    /**
     * Gets the latest forge version.
     *
     * @return the latest forge version, null if unknown
     */
    public ForgeVersion getLatest() {
        return latest;
    }

    /**
     * Gets the latest forge version of the given minecraft version.
     *
     * @param mcversion the minecraft version
     * @return the latest forge version of <code>mcversion</code>, null if
     * unknown
     */
    public ForgeVersion getLatest(String mcversion) {
        return latests.get(mcversion);
    }

    /**
     * Gets the recommended forge version.
     *
     * @return the recommended forge version, null if unknown
     */
    public ForgeVersion getRecommended() {
        return recommended;
    }

    /**
     * Gets the recommended forge version of the given minecraft version.
     *
     * @param mcversion the minecraft version
     * @return the recommended forge version of <code>mcversion</code>, null if
     * unknown
     */
    public ForgeVersion getRecommended(String mcversion) {
        return recommendeds.get(mcversion);
    }

    public Map<String, ForgeVersion> getForgeVersionMapping() {
        return forgeVersionMapping;
    }

    public ForgeVersion get(String mcversion, int buildNumber) {
        return versions.get(mcversion).get(buildNumber);
    }

    public ForgeVersion get(String forgeVersion) {
        return forgeVersionMapping.get(forgeVersion);
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
        if (obj instanceof ForgeVersionList) {
            ForgeVersionList another = (ForgeVersionList) obj;
            return Objects.equals(versions, another.versions) &&
                    Objects.equals(latests, another.latests) &&
                    Objects.equals(recommendeds, another.recommendeds) &&
                    Objects.equals(forgeVersionMapping, another.forgeVersionMapping) &&
                    Objects.equals(latest, another.latest) &&
                    Objects.equals(recommended, another.recommended);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("ForgeVersionList [versions=%s, latests=%s, recommendeds=%s, forgeVersionMapping=%s, latest=%s, recommended=%s]", versions, latests, recommendeds, forgeVersionMapping, latest, recommended);
    }

}
