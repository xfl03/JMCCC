package org.to2mbn.jmccc.version;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.to2mbn.jmccc.option.MinecraftDirectory;

public class Version implements Serializable {

	private static final long serialVersionUID = 1L;

	private String version;
	private String type;
	private String mainClass;
	private String assets;
	private String launchArgs;
	private String root;
	private Set<Library> libraries;
	private boolean legacy;
	private AssetIndexInfo assetIndexDownloadInfo;
	private Map<String, DownloadInfo> downloads;

	/**
	 * Constructor of Version.
	 * 
	 * @param version the version number
	 * @param type the type of the version, null if the type is unknown
	 * @param mainClass the main class
	 * @param assets the assets index name
	 * @param launchArgs the launch arguments
	 * @param root the root of the version hierarchy
	 * @param libraries the libraries to add to classpath
	 * @param legacy true if this version is lower than 1.7.10, as well as using
	 *            the legacy assets index
	 * @param assetIndexDownloadInfo the asset download info, can be null
	 * @param downloads the download infos, can be null
	 * @throws NullPointerException if any of the arguments (except type,
	 *             assetIndexDownloadInfo, downloads) is null
	 */
	public Version(String version, String type, String mainClass, String assets, String launchArgs, String root, Set<Library> libraries, boolean legacy, AssetIndexInfo assetIndexDownloadInfo, Map<String, DownloadInfo> downloads) {
		Objects.requireNonNull(version);
		Objects.requireNonNull(mainClass);
		Objects.requireNonNull(assets);
		Objects.requireNonNull(launchArgs);
		Objects.requireNonNull(root);
		Objects.requireNonNull(libraries);
		this.version = version;
		this.type = type;
		this.mainClass = mainClass;
		this.assets = assets;
		this.launchArgs = launchArgs;
		this.root = root;
		this.libraries = libraries;
		this.legacy = legacy;
		this.assetIndexDownloadInfo = assetIndexDownloadInfo;
		this.downloads = downloads;
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
	 * Gets the type of the version, e.g. "snapshot", "release", null if the type is unknown.
	 * 
	 * @return the type of the version, null if the type is unknown
	 */
	public String getType() {
		return type;
	}

	/**
	 * Gets the main class.
	 * 
	 * @return the main class
	 */
	public String getMainClass() {
		return mainClass;
	}

	/**
	 * Gets the assets index name.
	 * <p>
	 * Returns <code>"legacy"</code> if it's a legacy version.
	 * 
	 * @return the assets index name
	 * @see #isLegacy()
	 */
	public String getAssets() {
		return assets;
	}

	/**
	 * Gets the launch arguments.
	 * 
	 * @return the launch arguments
	 */
	public String getLaunchArgs() {
		return launchArgs;
	}

	/**
	 * Gets the root of the version hierarchy.
	 * <p>
	 * If this version does not have a super version('super version' is similar
	 * to 'superclass'), this value should be itself.<br>
	 * Each version uses the jar of its root version.
	 * 
	 * @return the root of the version hierarchy
	 */
	public String getRoot() {
		return root;
	}

	/**
	 * Gets the required libraries.
	 * 
	 * @return the required libraries
	 */
	public Set<Library> getLibraries() {
		return libraries;
	}

	/**
	 * Returns the missing libraries in the given minecraft directory.
	 * 
	 * @param minecraftDir the minecraft directory to check
	 * @return true the missing libraries in the given minecraft directory, an empty set if no library is missing
	 */
	public Set<Library> getMissingLibraries(MinecraftDirectory minecraftDir) {
		Set<Library> missing = new HashSet<>();
		for (Library library : libraries) {
			if (library.isMissing(minecraftDir)) {
				missing.add(library);
			}
		}
		return missing;
	}

	/**
	 * Returns true if the version is lower than 1.8
	 *
	 * @return true if the version is lower than 1.8, as well as using the legacy assets index
	 */
	public boolean isLegacy() {
		return legacy;
	}

	/**
	 * Gets the asset download info.
	 * 
	 * @return the asset download info, can be null
	 */
	public AssetIndexInfo getAssetIndexDownloadInfo() {
		return assetIndexDownloadInfo;
	}

	/**
	 * Gets the download information of the version.
	 * <p>
	 * This maps to the 'downloads' element in the version json.<br>
	 * Here are some known key-value pairs:<br>
	 * <code>client</code> -&gt; the client jar(minecraft client)<br>
	 * <code>server</code> -&gt; the server jar(minecraft server)<br>
	 * 
	 * @return the downloads of the version, can be null
	 */
	public Map<String, DownloadInfo> getDownloads() {
		return downloads;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Version) {
			Version another = (Version) obj;
			return version.equals(another.version) && Objects.equals(type, another.type) && mainClass.equals(another.mainClass) && assets.equals(another.assets) && launchArgs.equals(another.launchArgs) && root.equals(another.root) && libraries.equals(another.libraries) && legacy == another.legacy;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(version, type, mainClass, assets, launchArgs, root, libraries, legacy);
	}

	@Override
	public String toString() {
		return version;
	}

}
