package org.to2mbn.jmccc.mcdownloader;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class RemoteVersion implements Serializable {

	private static final long serialVersionUID = 1L;

	private String version;
	private Date uploadTime;
	private Date releaseTime;
	private String type;
	private String url;

	/**
	 * Constructor of RemoteVersion.
	 * 
	 * @param version the version number
	 * @param uploadTime the upload time
	 * @param releaseTime the release time
	 * @param type the version type
	 * @param url the url of the version json
	 * @throws NullPointerException if <code>version==null</code>
	 */
	public RemoteVersion(String version, Date uploadTime, Date releaseTime, String type, String url) {
		Objects.requireNonNull(version);
		this.version = version;
		this.uploadTime = uploadTime;
		this.releaseTime = releaseTime;
		this.type = type;
		this.url = url;
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
	 * Return values could be "snapshot", "release", "old_beta", "old_alpha"
	 * 
	 * @return the type of the version
	 */
	public String getType() {
		return type;
	}



	/**
	 * Gets the url of the version json, can be null.
	 * 
	 * @return the url of the version json, can be null
	 */
	public String getUrl() {
		return url;
	}

	@Override
	public String toString() {
		return String.format("RemoteVersion [version=%s, uploadTime=%s, releaseTime=%s, type=%s, url=%s]", version, uploadTime, releaseTime, type, url);
	}

	@Override
	public int hashCode() {
		return Objects.hash(version, uploadTime, releaseTime, type, url);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof RemoteVersion) {
			RemoteVersion another = (RemoteVersion) obj;
			return version.equals(another.version) &&
					Objects.equals(releaseTime, another.releaseTime) &&
					Objects.equals(uploadTime, another.uploadTime) &&
					Objects.equals(type, another.type) &&
					Objects.equals(url, another.url);
		}
		return false;
	}

}
