package org.to2mbn.jmccc.version;

import java.util.Objects;

public class LibraryInfo extends DownloadInfo {

	private static final long serialVersionUID = 1L;

	private String path;

	/**
	 * Constructor of LibraryInfo.
	 * 
	 * @param url the download url, null if the url is unknown
	 * @param checksum the SHA-1 checksum, null if the checksum is unknown
	 * @param size the file size, -1 if the size is unknown
	 * @param path the path of the library, null if the path is unknown
	 */
	public LibraryInfo(String url, String checksum, long size, String path) {
		super(url, checksum, size);
		this.path = path;
	}

	/**
	 * Gets the path of the library, null if the path is unknown.
	 * 
	 * @return the path of the library, null if the path is unknown
	 */
	public String getPath() {
		return path;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), path);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof LibraryInfo && super.equals(obj)) {
			LibraryInfo another = (LibraryInfo) obj;
			return Objects.equals(path, another.path);
		}
		return false;
	}

}
