package com.github.to2mbn.jmccc.mcdownloader;

public class RemoteVersion {

	private String verison;
	private String updateTime;
	private String releaseTime;
	private String type;

	public RemoteVersion(String verison, String updateTime, String releaseTime, String type) {
		this.verison = verison;
		this.updateTime = updateTime;
		this.releaseTime = releaseTime;
		this.type = type;
	}

	/**
	 * Gets the verison number.
	 * 
	 * @return the verison number
	 */
	public String getVerison() {
		return verison;
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

}
