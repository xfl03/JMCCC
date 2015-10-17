package com.github.to2mbn.jmccc.mcdownloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import com.github.to2mbn.jmccc.mcdownloader.util.HexUtils;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;

public class Asset {

	private String virtualPath;
	private String hash;
	private int size;

	/**
	 * Creates an asset.
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

	public File locate(MinecraftDirectory dir) {
		String subpath = "objects/" + hash.substring(0, 2) + "/" + hash;
		return new File(dir.getAssets(), subpath);
	}

	public boolean isValid(MinecraftDirectory dir) throws IOException, NoSuchAlgorithmException {
		File file = locate(dir);
		if (!file.isFile()) {
			return false;
		}

		if (file.length() != size) {
			return false;
		}

		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		try (InputStream in = new FileInputStream(file)) {
			byte[] buffer = new byte[8192];
			int read;
			while ((read = in.read(buffer)) != -1) {
				sha1.update(buffer, 0, read);
			}
		}

		return Arrays.equals(sha1.digest(), HexUtils.hexToBytes(hash));
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
