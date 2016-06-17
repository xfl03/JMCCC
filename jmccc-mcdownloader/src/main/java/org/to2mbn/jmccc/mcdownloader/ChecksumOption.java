package org.to2mbn.jmccc.mcdownloader;

public enum ChecksumOption implements MinecraftDownloadOption {

	/**
	 * Verify the checksums of libraries (only available when the checksum is
	 * defined in version.json).
	 */
	CHECK_LIBRAIES,

	/**
	 * Verify the checksums of assets.
	 */
	CHECK_ASSETS;

}
