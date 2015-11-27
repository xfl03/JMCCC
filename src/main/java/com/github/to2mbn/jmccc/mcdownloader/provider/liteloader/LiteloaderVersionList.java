package com.github.to2mbn.jmccc.mcdownloader.provider.liteloader;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import org.json.JSONObject;

public class LiteloaderVersionList {

	@SuppressWarnings("unchecked")
	public static LiteloaderVersionList fromJson(JSONObject json) {
		Map<String, Map<String, LiteloaderVersion>> versions = new TreeMap<>();
		JSONObject versionsJson = json.getJSONObject("versions");
		for (String mcversion : (Set<String>) versionsJson.keySet()) {
			Map<String, LiteloaderVersion> artefacts = new TreeMap<>();
			JSONObject artefactsJson = versionsJson.getJSONObject(mcversion).getJSONObject("artefacts");
			for (String artefactId : (Set<String>) artefactsJson.keySet()) {
				JSONObject artefactJson = artefactsJson.getJSONObject(artefactId);
				String liteloaderVersion = artefactJson.getString("version");
				String strdate = artefactJson.optString("timestamp", null);
				Date releaseDate = null;
				if (strdate != null) {
					releaseDate = new Date(Integer.parseInt(strdate));
				}
				artefacts.put(artefactId, new LiteloaderVersion(mcversion, liteloaderVersion, releaseDate));
			}
			versions.put(mcversion, artefacts);
		}
		return new LiteloaderVersionList(versions);
	}

	/**
	 * The outside map's key is the minecraft version, value is the artefacts.
	 * The inside map's key is the artefact name, value is artefact.
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
