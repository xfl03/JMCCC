package com.github.to2mbn.jmccc.mcdownloader;

import java.util.Map;

public class RemoteVersionList {

	private String latestSnapshot;
	private String latestRelease;
	private Map<String, RemoteVersion> versions;

	/**
	 * Creates a RemoteVersionList.
	 * 
	 * @param latestSnapshot the version of the latest snapshot
	 * @param latestRelease the version of the latestrelease
	 * @param versions the versions
	 */
	public RemoteVersionList(String latestSnapshot, String latestRelease, Map<String, RemoteVersion> versions) {
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

}
