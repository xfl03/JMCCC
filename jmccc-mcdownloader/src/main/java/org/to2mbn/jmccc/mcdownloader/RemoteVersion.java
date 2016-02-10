package org.to2mbn.jmccc.mcdownloader;

import java.util.Date;
import java.util.Objects;

public class RemoteVersion {

	private String version;
	private Date uploadTime;
	private Date releaseTime;
	private String type;

	/**
	 * Creates a RemoteVersion.
	 * 
	 * @param version the version number
	 * @param uploadTime the upload time
	 * @param releaseTime the release time
	 * @param type the version type
	 * @throws NullPointerException if <code>version==null</code>
	 */
	public RemoteVersion(String version, Date uploadTime, Date releaseTime, String type) {
		Objects.requireNonNull(version);
		this.version = version;
		this.uploadTime = uploadTime;
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
	 * Gets the upload time.
	 * 
	 * @return the upload time
	 */
	public Date getUploadTime() {
		return uploadTime;
	}

	/**
	 * Gets the release time.
	 * 
	 * @return the release time
	 */
	public Date getReleaseTime() {
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
		return version + " [uploadTime=" + uploadTime + ", releaseTime=" + releaseTime + ", type=" + type + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(version, uploadTime, releaseTime, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof RemoteVersion) {
			RemoteVersion another = (RemoteVersion) obj;
			return version.equals(another.version) && Objects.equals(releaseTime, another.releaseTime) && Objects.equals(uploadTime, another.uploadTime) && Objects.equals(type, another.type);
		}
		return false;
	}

}
