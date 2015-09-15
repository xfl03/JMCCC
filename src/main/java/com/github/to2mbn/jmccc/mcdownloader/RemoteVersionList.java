package com.github.to2mbn.jmccc.mcdownloader;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONObject;

public class RemoteVersionList {

	public static RemoteVersionList fromJson(JSONObject json) {
		String latestSnapshot = null;
		String latestRelease = null;
		if (json.has("latest")) {
			JSONObject latest = json.getJSONObject("latest");
			latestSnapshot = latest.optString("snapshot");
			latestRelease = latest.optString("release");
		}

		JSONArray jsonVersions = json.getJSONArray("versions");
		Map<String, RemoteVersion> versions = new HashMap<>();
		for (int i = 0; i < jsonVersions.length(); i++) {
			JSONObject jsonVersion = jsonVersions.getJSONObject(i);
			String version = jsonVersion.getString("id");
			String updateTime = jsonVersion.getString("time");
			String releaseTime = jsonVersion.getString("releaseTime");
			String type = jsonVersion.getString("type");
			versions.put(version, new RemoteVersion(version, updateTime, releaseTime, type));
		}
		return new RemoteVersionList(latestSnapshot, latestRelease, versions);
	}

	private String latestSnapshot;
	private String latestRelease;
	private Map<String, RemoteVersion> versions;

	/**
	 * Creates a RemoteVersionList.
	 * 
	 * @param latestSnapshot the version of the latest snapshot
	 * @param latestRelease the version of the latestrelease
	 * @param versions the versions
	 * @throws NullPointerException <code>versions==null</code>
	 */
	public RemoteVersionList(String latestSnapshot, String latestRelease, Map<String, RemoteVersion> versions) {
		Objects.requireNonNull(versions);
		this.latestSnapshot = latestSnapshot;
		this.latestRelease = latestRelease;
		this.versions = versions;
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

}
