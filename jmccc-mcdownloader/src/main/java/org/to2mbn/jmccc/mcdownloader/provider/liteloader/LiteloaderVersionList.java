package org.to2mbn.jmccc.mcdownloader.provider.liteloader;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import org.json.JSONArray;
import org.json.JSONObject;

public class LiteloaderVersionList implements Serializable {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public static LiteloaderVersionList fromJson(JSONObject json) {
		Map<String, Map<String, LiteloaderVersion>> versions = new TreeMap<>();
		JSONObject versionsJson = json.getJSONObject("versions");
		for (String mcversion : (Set<String>) versionsJson.keySet()) {
			Map<String, LiteloaderVersion> artefacts = new TreeMap<>();
			JSONObject versionRootJson = versionsJson.getJSONObject(mcversion);

			String repoUrl = null;
			JSONObject repoJson = versionRootJson.optJSONObject("repo");
			if (repoJson != null) {
				repoUrl = repoJson.optString("url", null);
			}

			JSONObject artefactsJson = versionRootJson.optJSONObject("artefacts");
			if (artefactsJson != null) {
				JSONObject liteloaderArtefactsJson = artefactsJson.getJSONObject("com.mumfrey:liteloader");
				for (String artefactId : (Set<String>) liteloaderArtefactsJson.keySet()) {
					JSONObject artefactJson = liteloaderArtefactsJson.getJSONObject(artefactId);
					String liteloaderVersion = artefactJson.getString("version");
					String tweakClass = artefactJson.optString("tweakClass", null);
					JSONArray librariesJson = artefactJson.optJSONArray("libraries");
					Set<JSONObject> libraries = null;
					if (librariesJson != null) {
						libraries = new HashSet<>();
						for (int i = 0; i < librariesJson.length(); i++)
							libraries.add(librariesJson.getJSONObject(i));
					}
					artefacts.put(artefactId, new LiteloaderVersion(mcversion, liteloaderVersion, tweakClass, repoUrl, Collections.unmodifiableSet(libraries)));
				}
				versions.put(mcversion, artefacts);
			}
		}
		return new LiteloaderVersionList(versions);
	}

	/**
	 * The outside map's key is the minecraft version, value is the artifacts.
	 * The inside map's key is the artifact name, value is artifact.
	 */
	private Map<String, Map<String, LiteloaderVersion>> versions;
	private Map<String, LiteloaderVersion> latests;

	public LiteloaderVersionList(Map<String, Map<String, LiteloaderVersion>> versions) {
		Objects.requireNonNull(versions);
		this.versions = versions;

		Map<String, LiteloaderVersion> latests = new TreeMap<>();
		for (Entry<String, Map<String, LiteloaderVersion>> entry : versions.entrySet()) {
			LiteloaderVersion latest = entry.getValue().get("latest");
			if (latest != null) {
				latests.put(entry.getKey(), latest);
			}
		}
		this.latests = Collections.unmodifiableMap(latests);
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
	 *         version
	 */
	public LiteloaderVersion getLatest(String minecraftVersion) {
		return getArtefact(minecraftVersion, "latest");
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

	@Override
	public String toString() {
		return "LiteloaderVersionList [" + versions + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(versions);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof LiteloaderVersionList) {
			LiteloaderVersionList another = (LiteloaderVersionList) obj;
			return versions.equals(another.versions);
		}
		return false;
	}

}
