package com.github.to2mbn.jmccc.mcdownloader;

import java.util.concurrent.Future;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloadGroupFutureCallback;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;
import com.github.to2mbn.jmccc.version.Library;

public interface MinecraftDownloader {

	/**
	 * Downloads the version list asynchronously.
	 * <p>
	 * This method downloads the version list to memory.
	 * <p>
	 * Exceptions:
	 * 
	 * <pre>
	 * IOException - if an i/o error occurs
	 * JSONException - if fails to resolve json
	 * </pre>
	 * 
	 * @param callback download callback
	 * @return future representing pending completion of the download
	 */
	Future<RemoteVersionList> downloadVersionList(DownloadGroupFutureCallback<RemoteVersionList> callback);

	/**
	 * Downloads the asset index asynchronously.
	 * <p>
	 * If <code>useCache==true</code> and <code>${mcdir}/assets/indexes/${version}</code> exists, this method will use
	 * the cached asset index. Else this method will download the asset index and caches it. If a json exception occurs
	 * during handling the cached asset index, this method will download the asset index again.
	 * <p>
	 * Exceptions:
	 * 
	 * <pre>
	 * IOException - if an i/o error occurs
	 * JSONException - if fails to resolve json
	 * </pre>
	 * 
	 * @param mcdir the minecraft dir
	 * @param version the asset index version
	 * @param useCache true if uses the cached asset index
	 * @param callback download callback
	 * @return future representing pending completion of the download
	 */
	Future<AssetsIndex> downloadAssetsIndex(MinecraftDirectory mcdir, String version, boolean useCache, DownloadGroupFutureCallback<AssetsIndex> callback);

	/**
	 * Downloads the game jar asynchronously.
	 * <p>
	 * The jar is saved to <code>${mcdir}/versions/${version}/${version}.jar</code>. If the file already exists, this
	 * method will overwrite the file.
	 * <p>
	 * Exceptions:
	 * 
	 * <pre>
	 * IOException - if an i/o error occurs
	 * JSONException - if fails to resolve json
	 * </pre>
	 * 
	 * @param mcdir the minecraft dir
	 * @param version the game version
	 * @param callback download callback
	 * @return future representing pending completion of the download
	 */
	Future<?> downloadGameJar(MinecraftDirectory mcdir, String version, DownloadGroupFutureCallback<?> callback);

	/**
	 * Downloads the library asynchronously.
	 * <p>
	 * The library is saved to <code>${mcdir}/libraries/${library.getPath()}</code>. If the file already exists, this
	 * method will overwrite the file.
	 * <p>
	 * Exceptions:
	 * 
	 * <pre>
	 * IOException - if an i/o error occurs
	 * JSONException - if fails to resolve json
	 * ChecksumException - if the checksum mismatches
	 * </pre>
	 * 
	 * @param mcdir the minecraft dir
	 * @param library the library to download
	 * @param callback download callback
	 * @return future representing pending completion of the download
	 */
	Future<?> downloadLibrary(MinecraftDirectory mcdir, Library library, DownloadGroupFutureCallback<?> callback);

	/**
	 * Downloads the asset asynchronously.
	 * <p>
	 * The library is saved to <code>${mcdir}/libraries/${library.getPath()}</code>. If the file already exists, this
	 * method will overwrite the file.
	 * <p>
	 * Exceptions:
	 * 
	 * <pre>
	 * IOException - if an i/o error occurs
	 * JSONException - if fails to resolve json
	 * ChecksumException - if the checksum mismatches
	 * </pre>
	 * 
	 * @param mcdir the minecraft dir
	 * @param asset the asset to download
	 * @param callback download callback
	 * @return future representing pending completion of the download
	 */
	Future<?> downloadAsset(MinecraftDirectory mcdir, Asset asset, DownloadGroupFutureCallback<?> callback);

}
