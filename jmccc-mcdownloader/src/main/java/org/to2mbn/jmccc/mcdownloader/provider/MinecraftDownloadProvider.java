package org.to2mbn.jmccc.mcdownloader.provider;

import java.util.Set;
import org.to2mbn.jmccc.mcdownloader.RemoteVersionList;
import org.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloadTask;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.Library;

public interface MinecraftDownloadProvider {

	/**
	 * Returns a version list download task.
	 * 
	 * @return a version list download task
	 */
	MultipleDownloadTask<RemoteVersionList> versionList();

	/**
	 * Returns an asset index download task.
	 * <p>
	 * The asset index will also be saved to <code>${mcdir}/indexes/${version}.json</code>.
	 * 
	 * @param mcdir the minecraft dir
	 * @param version the asset index version
	 * @return an asset index download task
	 */
	MultipleDownloadTask<Set<Asset>> assetsIndex(MinecraftDirectory mcdir, String version);

	/**
	 * Returns a game jar download task.
	 * <p>
	 * The jar is saved to <code>${mcdir}/versions/${version}/${version}.jar</code>. If the file already exists, this
	 * method will overwrite the file.
	 * 
	 * @param mcdir the minecraft dir
	 * @param version the game version
	 * @return a game jar download task
	 */
	MultipleDownloadTask<Object> gameJar(MinecraftDirectory mcdir, String version);

	/**
	 * Returns a game version json download task.
	 * <p>
	 * The version json is saved to <code>${mcdir}/versions/${version}/${version}.json</code>. If the file already
	 * exists, this method will overwrite the file.
	 * 
	 * @param mcdir the minecraft dir
	 * @param version the game version
	 * @return a version json download task
	 */
	MultipleDownloadTask<Object> gameVersionJson(MinecraftDirectory mcdir, String version);

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
	MultipleDownloadTask<Object> library(MinecraftDirectory mcdir, Library library);

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
	MultipleDownloadTask<Object> asset(MinecraftDirectory mcdir, Asset asset);

}
