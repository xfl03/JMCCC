package org.to2mbn.jmccc.version;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.json.JSONException;
import org.to2mbn.jmccc.option.MinecraftDirectory;

/**
 * A tool class for resolving versions.
 * 
 * @author yushijinhun
 */
public final class Versions {

	private static final VersionParser PARSER = new VersionParser();

	/**
	 * Resolves the version.
	 * 
	 * @param minecraftDir the minecraft directory
	 * @param version the version name
	 * @return the version object, null if the version does not exist
	 * @throws IOException if an I/O error has occurred during resolving version
	 * @throws NullPointerException if
	 *             <code>minecraftDir==null || version==null</code>
	 */
	public static Version resolveVersion(MinecraftDirectory minecraftDir, String version) throws IOException {
		Objects.requireNonNull(minecraftDir);
		Objects.requireNonNull(version);

		if (doesVersionExist(minecraftDir, version)) {
			try {
				return PARSER.parseVersion(minecraftDir, version);
			} catch (JSONException e) {
				throw new IOException("unable to resolve json", e);
			}
		} else {
			return null;
		}
	}

	/**
	 * Returns the set of versions in the given minecraft directory.
	 * <p>
	 * This method returns a non-threaded safe, unordered set.
	 * 
	 * @param minecraftDir the minecraft directory
	 * @return the set of versions
	 * @throws NullPointerException if <code>minecraftDir==null</code>
	 */
	public static Set<String> getVersions(MinecraftDirectory minecraftDir) {
		Objects.requireNonNull(minecraftDir);
		Set<String> versions = new HashSet<>();

		// null if the 'versions' dir not exists
		File[] subdirs = minecraftDir.getVersions().listFiles();
		if (subdirs != null) {
			for (File file : subdirs) {
				if (file.isDirectory() && doesVersionExist(minecraftDir, file.getName())) {
					versions.add(file.getName());
				}
			}
		}
		return versions;
	}

	/**
	 * Resolves the asset index.
	 * 
	 * @param minecraftDir the minecraft directory
	 * @param version the owner version of the asset index
	 * @return the asset index, null if the asset index does not exist
	 * @throws IOException if an I/O error occurs during resolving asset index
	 * @throws NullPointerException if
	 *             <code>minecraftDir==null || version==null</code>
	 */
	public static Set<Asset> resolveAssets(MinecraftDirectory minecraftDir, Version version) throws IOException {
		return resolveAssets(minecraftDir, version.getAssets());
	}

	/**
	 * Resolves the asset index.
	 * 
	 * @param minecraftDir the minecraft directory
	 * @param assets the name of the asset index, you can get this via
	 *            {@link Version#getAssets()}
	 * @return the asset index, null if the asset index does not exist
	 * @throws IOException if an I/O error has occurred during resolving asset
	 *             index
	 * @throws NullPointerException if
	 *             <code>minecraftDir==null || assets==null</code>
	 */
	public static Set<Asset> resolveAssets(MinecraftDirectory minecraftDir, String assets) throws IOException {
		Objects.requireNonNull(minecraftDir);
		Objects.requireNonNull(assets);
		if (!minecraftDir.getAssetIndex(assets).isFile()) {
			return null;
		}

		try {
			return PARSER.parseAssets(minecraftDir, assets);
		} catch (JSONException e) {
			throw new IOException("unable to resolve json", e);
		}
	}

	private static boolean doesVersionExist(MinecraftDirectory minecraftDir, String version) {
		return minecraftDir.getVersionJson(version).isFile();
	}

	private Versions() {
	}

}
