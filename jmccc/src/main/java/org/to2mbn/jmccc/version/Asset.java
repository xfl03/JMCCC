package org.to2mbn.jmccc.version;

import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.util.ChecksumUtils;

public class Asset implements Serializable {

	private static final long serialVersionUID = 1L;

	private String virtualPath;
	private String hash;
	private int size;

	/**
	 * Constructor of Asset.
	 * 
	 * @param virtualPath the virtual path
	 * @param hash the sha1 hash
	 * @param size the size
	 * @throws NullPointerException if <code>virtualPath==null||hash==null</code>
	 * @throws IllegalArgumentException if <code>size&lt;0</code>
	 */
	public Asset(String virtualPath, String hash, int size) {
		Objects.requireNonNull(virtualPath);
		Objects.requireNonNull(hash);
		if (size < 0) {
			throw new IllegalArgumentException("size<0");
		}

		this.virtualPath = virtualPath;
		this.hash = hash;
		this.size = size;
	}

	/**
	 * Gets the virtual path.
	 * 
	 * @return the virtual path
	 */
	public String getVirtualPath() {
		return virtualPath;
	}

	/**
	 * Gets the sha1 hash of the asset.
	 * 
	 * @return the sha1 hash of the asset
	 */
	public String getHash() {
		return hash;
	}

	/**
	 * Gets the size of the asset.
	 * 
	 * @return the size of the asset
	 */
	public int getSize() {
		return size;
	}

	@Override
	public String toString() {
		return virtualPath + " [hash=" + hash + ", size=" + size + "]";
	}

	/**
	 * Gets the relative path of the asset.
	 * <p>
	 * The method uses '/' separator, and uses 'assets/objects' as the base dir.<br>
	 * The asset file is located at:
	 * 
	 * <pre>
	 * ${mcdir}/assets/objects/${2-character-prefix of hash}/${hash}
	 * </pre>
	 * 
	 * So the format of the return value will be:
	 * 
	 * <pre>
	 * ${2-character-prefix of hash}/${hash}
	 * </pre>
	 * 
	 * @return the relative path of the asset
	 */
	public String getPath() {
		return hash.substring(0, 2) + "/" + hash;
	}

	/**
	 * Validates the asset in the given mcdir.
	 * <p>
	 * This method checks the size, hash of the asset. If, one of them mismatches, or the asset file doesn't exist, this
	 * method will return false. Or this method will return true.
	 * 
	 * @param dir the mcdir where the asset is in
	 * @return true if the asset is valid
	 * @throws IOException if an i/o error occurs
	 * @throws NoSuchAlgorithmException if the default hash algorithm SHA-1 doesn't exist
	 */
	public boolean isValid(MinecraftDirectory dir) throws IOException, NoSuchAlgorithmException {
		return ChecksumUtils.verify(dir.getAsset(this), getHash(), "SHA-1", size);
	}

	@Override
	public int hashCode() {
		return Objects.hash(virtualPath, hash, size);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof Asset) {
			Asset another = (Asset) obj;
			return virtualPath.equals(another.virtualPath) && hash.equals(another.hash) && size == another.size;
		}
		return false;
	}

}
