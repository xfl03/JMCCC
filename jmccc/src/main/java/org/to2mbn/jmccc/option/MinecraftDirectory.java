package org.to2mbn.jmccc.option;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Version;

/**
 * Describes a minecraft directory.
 * 
 * @author yushijinhun
 */
public class MinecraftDirectory implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The '.minecraft' dir.
	 */
	protected File rootDir;

	/**
	 * Creates a MinecraftDirectory with the '.minecraft' directory in the current directory.
	 */
	public MinecraftDirectory() {
		this(new File(".minecraft"));
	}

	/**
	 * Creates a MinecraftDirectory with the given root directory.
	 * 
	 * @param rootDir the root directory of minecraft (eg. <code>".minecraft"</code>)
	 */
	public MinecraftDirectory(String rootDir) {
		this(new File(rootDir));
	}

	/**
	 * Creates a MinecraftDirectory with the given root directory.
	 * 
	 * @param rootDir the root directory of minecraft (eg. <code>".minecraft"</code>)
	 */
	public MinecraftDirectory(File rootDir) {
		Objects.requireNonNull(rootDir);
		this.rootDir = rootDir.getAbsoluteFile();
	}

	/**
	 * Gets the root of the minecraft directory.
	 * <p>
	 * Usually it's '.minecraft'.
	 * 
	 * @return the root of the minecraft directory
	 */
	public File getRoot() {
		return rootDir;
	}

	/**
	 * Gets the versions directory.
	 * 
	 * @return the versions directory
	 */
	public File getVersions() {
		return new File(rootDir, "versions");
	}

	/**
	 * Gets the libraries directory.
	 * 
	 * @return the libraries directory
	 */
	public File getLibraries() {
		return new File(rootDir, "libraries");
	}

	/**
	 * Gets the library file.
	 * 
	 * @param library the library
	 * @return the library file
	 */
	public File getLibrary(Library library) {
		return new File(getLibraries(), library.getPath());
	}

	/**
	 * Gets the natives directory.
	 * 
	 * @param version the owner of the natives
	 * @return the natives directory
	 */
	public File getNatives(String version) {
		return new File(getVersion(version), version + "-natives");
	}

	/**
	 * Gets the natives directory.
	 * 
	 * @param version the owner of the natives
	 * @return the natives directory
	 */
	public File getNatives(Version version) {
		return getNatives(version.getRoot());
	}

	/**
	 * Gets the assets directory.
	 * 
	 * @return the assets directory
	 */
	public File getAssets() {
		return new File(rootDir, "assets");
	}

	/**
	 * Gets the virtual assets directory.
	 * 
	 * @return the virtual assets directory
	 */
	public File getVirtualAssets() {
		return new File(getAssets(), "virtual");
	}

	/**
	 * Gets the virtual legacy assets directory.
	 * 
	 * @return the virtual legacy assets directory
	 */
	public File getVirtualLegacyAssets() {
		return new File(getVirtualAssets(), "legacy");
	}

	/**
	 * Gets the object assets directory.
	 * 
	 * @return the object assets directory
	 */
	public File getAssetObjects() {
		return new File(getAssets(), "objects");
	}

	/**
	 * Gets the asset indexes directory.
	 * 
	 * @return the asset indexes directory
	 */
	public File getAssetIndexes() {
		return new File(getAssets(), "indexes");
	}

	/**
	 * Gets the asset index file.
	 * 
	 * @param assets the name of the asset index
	 * @return the asset index file
	 */
	public File getAssetIndex(String assets) {
		return new File(getAssetIndexes(), assets + ".json");
	}

	/**
	 * Gets the asset index file of the given version.
	 * 
	 * @param version the version
	 * @return the asset index file
	 */
	public File getAssetIndex(Version version) {
		return getAssetIndex(version.getAssets());
	}

	/**
	 * Gets the base directory of the given version
	 * 
	 * @param version the version
	 * @return the base directory of the given version
	 */
	public File getVersion(String version) {
		return new File(getVersions(), version);
	}

	/**
	 * Gets the json file of the given version
	 * 
	 * @param version the version
	 * @return the json file of the given version
	 */
	public File getVersionJson(String version) {
		return new File(getVersion(version), version + ".json");
	}

	/**
	 * Gets the json file of the given version
	 * 
	 * @param version the version
	 * @return the json file of the given version
	 */
	public File getVersionJson(Version version) {
		return getVersionJson(version.getVersion());
	}

	/**
	 * Gets the jar file of the given version
	 * 
	 * @param version the version
	 * @return the jar file of the given version
	 */
	public File getVersionJar(String version) {
		return new File(getVersion(version), version + ".jar");
	}

	/**
	 * Gets the jar file of the given version
	 * 
	 * @param version the version
	 * @return the jar file of the given version
	 */
	public File getVersionJar(Version version) {
		return getVersionJar(version.getRoot());
	}

	/**
	 * Gets the location of the given asset.
	 * 
	 * @param asset the asset
	 * @return the location of the asset
	 */
	public File getAsset(Asset asset) {
		return new File(getAssetObjects(), asset.getPath());
	}

	/**
	 * Gets the virtual location of the given asset.
	 * 
	 * @param asset the asset
	 * @return the virtual location of the asset
	 */
	public File getVirtualAsset(Asset asset) {
		return new File(getVirtualLegacyAssets(), asset.getVirtualPath());
	}

	@Override
	public String toString() {
		return rootDir.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof MinecraftDirectory) {
			MinecraftDirectory another = (MinecraftDirectory) obj;
			return rootDir.equals(another.rootDir);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return rootDir.hashCode();
	}

}
