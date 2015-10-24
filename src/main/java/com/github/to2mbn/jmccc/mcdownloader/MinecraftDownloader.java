package com.github.to2mbn.jmccc.mcdownloader;

import java.util.Set;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;
import com.github.to2mbn.jmccc.version.Library;

public interface MinecraftDownloader {

	/**
	 * Returns a version list download task.
	 * 
	 * @return a version list download task
	 */
	DownloadTask<RemoteVersionList> versionList();

	/**
	 * Returns an asset index download task.
	 * <p>
	 * The asset index will also be saved to <code>${mcdir}/indexes/${version}.json</code>.
	 * 
	 * @param mcdir the minecraft dir
	 * @param version the asset index version
	 * @return an asset index download task
	 */
	DownloadTask<Set<Asset>> assetsIndex(MinecraftDirectory mcdir, String version);

	/**
	 * Returns a game download task.
	 * <p>
	 * The jar is saved to <code>${mcdir}/versions/${version}/${version}.jar</code>. The version json is saved to
	 * <code>${mcdir}/versions/${version}/${version}.json</code>. If the file already exists, this method will overwrite
	 * the file.
	 * 
	 * @param mcdir the minecraft dir
	 * @param version the game version
	 * @return a game download task
	 */
	DownloadTask<?> game(MinecraftDirectory mcdir, String version);

	/**
	 * Returns a library download task.
	 * <p>
	 * The library is saved to <code>${mcdir}/libraries/${library.getPath()}</code>. If the file already exists, this
	 * method will overwrite the file.
	 * 
	 * @param mcdir the minecraft dir
	 * @param library the library to download
	 * @return a library download task
	 */
	DownloadTask<?> library(MinecraftDirectory mcdir, Library library);

	/**
	 * Returns an asset download task.
	 * <p>
	 * The asset is saved to <code>${mcdir}/assets/objects/${2-character-prefix of hash}/${hash}</code>. If the file
	 * already exists, this method will overwrite the file.
	 * 
	 * @param mcdir the minecraft dir
	 * @param asset the asset to download
	 * @return an asset download task
	 */
	DownloadTask<?> asset(MinecraftDirectory mcdir, Asset asset);

}
