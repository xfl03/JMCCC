package org.to2mbn.jmccc.mcdownloader.provider.forge;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class ForgeVersionList implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(ForgeVersionList.class.getCanonicalName());

	@SuppressWarnings("unchecked")
	public static ForgeVersionList fromJson(JSONObject json) {
		Map<Integer, ForgeVersion> versions = new TreeMap<>();
		Map<String, ForgeVersion> latests = new TreeMap<>();
		Map<String, ForgeVersion> recommendeds = new TreeMap<>();
		Map<String, ForgeVersion> forgeVersionMapping = new TreeMap<>();
		ForgeVersion latest = null;
		ForgeVersion recommended = null;

		JSONObject versionsJson = json.getJSONObject("number");
		for (String strbuildnum : (Set<String>) versionsJson.keySet()) {
			JSONObject versionJson = versionsJson.getJSONObject(strbuildnum);
			try {
				String mcversion = versionJson.getString("mcversion");
				String forgeversion = versionJson.getString("version");
				int buildnum = versionJson.getInt("build");
				String branch = versionJson.optString("branch", null);
				ForgeVersion version = new ForgeVersion(mcversion, forgeversion, buildnum, branch);

				versions.put(buildnum, version);
				forgeVersionMapping.put(forgeversion, version);
			} catch (JSONException e) {
				LOGGER.log(Level.WARNING, "Couldn't parse forge version, skipping: " + versionJson, e);
			}
		}
		JSONObject promos = json.getJSONObject("promos");
		for (String key : (Set<String>) promos.keySet()) {
			ForgeVersion version = versions.get(promos.getInt(key));
			if ("latest".equals(key)) {
				latest = version;
			} else if ("recommended".equals(key)) {
				recommended = version;
			} else if (key.endsWith("-latest")) {
				// 7 - length of "-latest"
				latests.put(key.substring(0, key.length() - 7), version);
			} else if (key.endsWith("-recommended")) {
				// 12 - length of "-recommended"
				recommendeds.put(key.substring(0, key.length() - 12), version);
			}
		}
		return new ForgeVersionList(Collections.unmodifiableMap(versions),
				Collections.unmodifiableMap(latests),
				Collections.unmodifiableMap(recommendeds),
				Collections.unmodifiableMap(forgeVersionMapping),
				latest,
				recommended);
	}

	private Map<Integer, ForgeVersion> versions;
	private Map<String, ForgeVersion> latests;
	private Map<String, ForgeVersion> recommendeds;
	private Map<String, ForgeVersion> forgeVersionMapping;
	private ForgeVersion latest;
	private ForgeVersion recommended;

	public ForgeVersionList(Map<Integer, ForgeVersion> versions, Map<String, ForgeVersion> latests, Map<String, ForgeVersion> recommendeds, Map<String, ForgeVersion> forgeVersionMapping, ForgeVersion latest, ForgeVersion recommended) {
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

	/**
	 * Gets all the forge versions.
	 * 
	 * @return all the forge versions, key is the build number
	 */
	public Map<Integer, ForgeVersion> getVersions() {
		return versions;
	}

	/**
	 * Gets all the latest versions.
	 * 
	 * @return a map including all the latest versions, key is the minecraft
	 *         version, value is the latest forge version of the minecraft
	 *         version
	 */
	public Map<String, ForgeVersion> getLatests() {
		return latests;
	}

	/**
	 * Gets all the recommended versions.
	 * 
	 * @return a map including all the recommended versions, key is the
	 *         minecraft version, value is the recommended forge version of the
	 *         minecraft version
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
	 *         unknown
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
	 *         unknown
	 */
	public ForgeVersion getRecommended(String mcversion) {
		return recommendeds.get(mcversion);
	}

	public Map<String, ForgeVersion> getForgeVersionMapping() {
		return forgeVersionMapping;
	}

	public ForgeVersion get(int buildNumber) {
		return versions.get(buildNumber);
	}

	public ForgeVersion get(String forgeVersion) {
		return forgeVersionMapping.get(forgeVersion);
	}

	@Override
	public int hashCode() {
		return Objects.hash(versions, latests, recommendeds, forgeVersionMapping, latest, recommended);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof ForgeVersionList) {
			ForgeVersionList another = (ForgeVersionList) obj;
			return versions.equals(another.versions) &&
					latests.equals(another.latests) &&
					recommendeds.equals(another.recommendeds) &&
					forgeVersionMapping.equals(another.forgeVersionMapping) &&
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
