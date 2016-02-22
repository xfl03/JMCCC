package org.to2mbn.jmccc.mcdownloader.provider.liteloader;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
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
			JSONObject artefactsJson = versionsJson.getJSONObject(mcversion).optJSONObject("artefacts");
			if (artefactsJson != null) {
				JSONObject liteloaderArtefactsJson = artefactsJson.getJSONObject("com.mumfrey:liteloader");
				for (String artefactId : (Set<String>) liteloaderArtefactsJson.keySet()) {
					JSONObject artefactJson = liteloaderArtefactsJson.getJSONObject(artefactId);
					String liteloaderVersion = artefactJson.getString("version");
					String file = artefactJson.optString("file", null);
					String md5 = artefactJson.optString("md5", null);
					String timestampStr = artefactJson.optString("timestamp", null);
					Long timestamp = timestampStr == null ? null : Long.valueOf(timestampStr);
					String tweakClass = artefactJson.optString("tweakClass", null);
					JSONArray librariesJson = artefactJson.optJSONArray("libraries");
					Set<JSONObject> libraries = null;
					if (librariesJson != null) {
						libraries = new HashSet<>();
						for (int i = 0; i < librariesJson.length(); i++)
							libraries.add(librariesJson.getJSONObject(i));
					}
					artefacts.put(artefactId, new LiteloaderVersion(mcversion, liteloaderVersion, file, md5, timestamp, tweakClass, Collections.unmodifiableSet(libraries)));
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

	public LiteloaderVersionList(Map<String, Map<String, LiteloaderVersion>> versions) {
		Objects.requireNonNull(versions);
		this.versions = versions;
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

	public LiteloaderVersion getLatestArtefact(String minecraftVersion) {
		return getArtefact(minecraftVersion, "latest");
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
