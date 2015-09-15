package com.github.to2mbn.jmccc.mcdownloader;

public class RemoteVersion {

	private String version;
	private String updateTime;
	private String releaseTime;
	private String type;

	public RemoteVersion(String version, String updateTime, String releaseTime, String type) {
		this.version = version;
		this.updateTime = updateTime;
		this.releaseTime = releaseTime;
		this.type = type;
	}

	/**
	 * Gets the version number.
	 * 
	 * @return the version number
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Gets the update time.
	 * <p>
	 * Format: yyyy-mm-ddThh:mm:ss+HH:MM<br>
	 * eg: 2009-05-13T22:11:00+02:00<br>
	 * 'HH:MM' is the timezone offest.
	 * 
	 * @return the update time
	 */
	public String getUpdateTime() {
		return updateTime;
	}

	/**
	 * Gets the release time.
	 * <p>
	 * Format: yyyy-mm-ddThh:mm:ss+HH:MM<br>
	 * eg: 2009-05-13T22:11:00+02:00<br>
	 * 'HH:MM' is the timezone offest.
	 * 
	 * @return the release time
	 */
	public String getReleaseTime() {
		return releaseTime;
	}

	/**
	 * Gets the type of the version.
	 * <p>
	 * Values can be "snapshot", "release", "old_beta", "old_alpha"
	 * 
	 * @return the type of the version
	 */
	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return "[version=" + version + ", updateTime=" + updateTime + ", releaseTime=" + releaseTime + ", type=" + type + "]";
	}

}
