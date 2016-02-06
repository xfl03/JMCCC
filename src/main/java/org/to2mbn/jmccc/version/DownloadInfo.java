package org.to2mbn.jmccc.version;

import java.io.Serializable;
import java.util.Objects;

public class DownloadInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String url;
	private String checksum;
	private long size;

	/**
	 * Creates a DownloadInfo.
	 * 
	 * @param url the url
	 * @param checksum the SHA-1 checksum, null if the checksum is unknown
	 * @param size the size
	 * @throws NullPointerException if <code>url==null</code>
	 */
	public DownloadInfo(String url, String checksum, long size) {
		Objects.requireNonNull(url);
		Objects.requireNonNull(checksum);
		this.url = url;
		this.checksum = checksum;
		this.size = size;
	}

	/**
	 * Gets the download url.
	 * 
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Gets the SHA-1 checksum.
	 * 
	 * @return the SHA-1 checksum, null if the checksum is unknown
	 */
	public String getChecksum() {
		return checksum;
	}

	/**
	 * Gets the file size.
	 * 
	 * @return the file size
	 */
	public long getSize() {
		return size;
	}

	@Override
	public int hashCode() {
		return Objects.hash(url, checksum, size);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof DownloadInfo) {
			DownloadInfo another = (DownloadInfo) obj;
			return url.equals(another.url) && Objects.equals(checksum, another.checksum) && size == another.size;
		}
		return false;
	}

	@Override
	public String toString() {
		return url;
	}

}
