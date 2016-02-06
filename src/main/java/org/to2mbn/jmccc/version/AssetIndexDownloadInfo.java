package org.to2mbn.jmccc.version;

import java.util.Objects;

public class AssetIndexDownloadInfo extends DownloadInfo {

	private static final long serialVersionUID = 1L;

	private String id;
	private long totalSize;
	private boolean known;

	/**
	 * Creates an AssetIndexDownloadInfo.
	 * 
	 * @param url the url
	 * @param checksum the SHA-1 checksum, null if the checksum is unknown
	 * @param size the size
	 * @param id the version of the asset index
	 * @param totalSize dunno wtf it is, ask mojang
	 * @param known true if the checksum and size are known
	 * @throws NullPointerException if <code>url==null || id==null</code>
	 */
	public AssetIndexDownloadInfo(String url, String checksum, long size, String id, long totalSize, boolean known) {
		super(url, checksum, size);
		Objects.requireNonNull(id);
		this.id = id;
		this.totalSize = totalSize;
		this.known = known;
	}

	/**
	 * Gets the version of the asset index.
	 * 
	 * @return the version of the asset index.
	 */
	public String getId() {
		return id;
	}

	/**
	 * The 'totalSize' element in the json, dunno wtf it is.
	 * 
	 * @return the 'totalSize' element in the json
	 */
	public long getTotalSize() {
		return totalSize;
	}

	/**
	 * Gets whether the checksum and size are known.
	 * 
	 * @return true if the checksum and size are known
	 */
	public boolean isKnown() {
		return known;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), id, totalSize, known);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof AssetIndexDownloadInfo && super.equals(obj)) {
			AssetIndexDownloadInfo another = (AssetIndexDownloadInfo) obj;
			return id.equals(another.id) && totalSize == another.totalSize && known == another.known;
		}
		return false;
	}

}
