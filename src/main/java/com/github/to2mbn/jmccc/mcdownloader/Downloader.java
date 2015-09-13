package com.github.to2mbn.jmccc.mcdownloader;

import java.util.concurrent.CompletableFuture;
import com.github.to2mbn.jmccc.version.Library;

public interface Downloader {

	/**
	 * Fetches the remote versions list asynchronously.
	 * 
	 * @return the remote versions list
	 */
	CompletableFuture<RemoteVersionList> getRemoteVersionList();

	/**
	 * Downloads the given library asynchronously.
	 * 
	 * @param library the library to download
	 * @return void
	 */
	CompletableFuture<Void> downloadLibrary(Library library);

	/**
	 * Downloads a minecraft asynchronously.
	 * 
	 * @param version the version of the minecraft to download
	 * @return void
	 */
	CompletableFuture<Void> downloadVersion(String version);

	/**
	 * Downloads the assets asynchronously.
	 * <p>
	 * This method will check the hashes of the assets first, and then download the missing or broken assets.
	 * 
	 * @param version the version of the assets
	 * @return void
	 */
	CompletableFuture<Void> downloadAssets(String version);

}
