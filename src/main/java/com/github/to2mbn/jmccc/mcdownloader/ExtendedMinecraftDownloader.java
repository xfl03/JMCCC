package com.github.to2mbn.jmccc.mcdownloader;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;
import com.github.to2mbn.jmccc.version.Library;
import com.github.to2mbn.jmccc.version.Version;

public class ExtendedMinecraftDownloader implements MinecraftDownloader {

	public static ExtendedMinecraftDownloader extend(MinecraftDownloader downloader) {
		if (downloader instanceof ExtendedMinecraftDownloader) {
			return (ExtendedMinecraftDownloader) downloader;
		}
		return new ExtendedMinecraftDownloader(downloader);
	}

	private MinecraftDownloader proxied;

	public ExtendedMinecraftDownloader(MinecraftDownloader proxied) {
		this.proxied = proxied;
	}

	@Override
	public DownloadTask<RemoteVersionList> versionList() {
		return proxied.versionList();
	}

	@Override
	public DownloadTask<Set<Asset>> assetsIndex(MinecraftDirectory mcdir, String version) {
		return proxied.assetsIndex(mcdir, version);
	}

	@Override
	public DownloadTask<Object> gameJar(MinecraftDirectory mcdir, String version) {
		return proxied.gameJar(mcdir, version);
	}

	@Override
	public DownloadTask<Object> gameVersionJson(MinecraftDirectory mcdir, String version) {
		return proxied.gameVersionJson(mcdir, version);
	}

	@Override
	public DownloadTask<Object> library(MinecraftDirectory mcdir, Library library) {
		return proxied.library(mcdir, library);
	}

	@Override
	public DownloadTask<Object> asset(MinecraftDirectory mcdir, Asset asset) {
		return proxied.asset(mcdir, asset);
	}

	/**
	 * Creates download tasks for broken assets.
	 * <p>
	 * This method validates the given assets, and returns a set of download tasks of the broken assets.
	 * 
	 * @param mcdir the minecraft dir
	 * @param assets the assets to check
	 * @return a set of download tasks of the broken assets
	 * @throws IOException if an I/O exception occurs during calling {@link Asset#isValid(MinecraftDirectory)}
	 * @throws NoSuchAlgorithmException if the default hash algorithm SHA-1 doesn't exist
	 */
	public Set<DownloadTask<Object>> missingAssets(MinecraftDirectory mcdir, Set<Asset> assets) throws IOException, NoSuchAlgorithmException {
		Set<DownloadTask<Object>> result = new HashSet<>();
		for (Asset asset : assets) {
			if (!asset.isValid(mcdir)) {
				result.add(asset(mcdir, asset));
			}
		}
		return result;
	}

	/**
	 * Creates download tasks for missing libraries.
	 * <p>
	 * This method checks the given libraries, and returns a set of download tasks of the missing libraries.
	 * 
	 * @param mcdir the minecraft dir
	 * @param libraries the libraries to check
	 * @return a set of download tasks of the missing libraries
	 */
	public Set<DownloadTask<Object>> missingLibraries(MinecraftDirectory mcdir, Set<Library> libraries) {
		Set<DownloadTask<Object>> result = new HashSet<>();
		for (Library library : libraries) {
			if (library.isMissing(mcdir)) {
				result.add(library(mcdir, library));
			}
		}
		return result;
	}

	/**
	 * Creates download tasks for missing libraries.
	 * <p>
	 * This method checks the given version, and returns a set of download tasks of the missing libraries.
	 * 
	 * @param mcdir the minecraft dir
	 * @param version the version to check
	 * @return a set of download tasks of the missing libraries
	 */
	public Set<DownloadTask<Object>> missingLibraries(MinecraftDirectory mcdir, Version version) {
		return missingLibraries(mcdir, version.getLibraries());
	}

	/**
	 * Creates download tasks for game jar and version json.
	 * <p>
	 * The returned set includes a game jar download task and a version json download task
	 * 
	 * @param mcdir the minecraft dir
	 * @param version the game version
	 * @return a set including a game jar download task and a version json download task
	 */
	public Set<DownloadTask<Object>> game(MinecraftDirectory mcdir, String version) {
		Set<DownloadTask<Object>> result = new HashSet<>();
		result.add(gameVersionJson(mcdir, version));
		result.add(gameJar(mcdir, version));
		return result;
	}

}
