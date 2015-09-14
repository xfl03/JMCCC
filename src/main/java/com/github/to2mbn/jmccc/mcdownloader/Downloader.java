package com.github.to2mbn.jmccc.mcdownloader;

import java.util.concurrent.CompletableFuture;
import com.github.to2mbn.jmccc.version.Library;

public interface Downloader {

	/**
	 * Fetches the remote versions list asynchronously.
	 * 
	 * @return the remote versions list
	 */
	CompletableFuture<RemoteVersionList> downloadRemoteVersionList();

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
	 * Downloads the given asset asynchronously.
	 * 
	 * @param asset the asset to download
	 * @return void
	 */
	CompletableFuture<Void> downloadAsset(Asset asset);

	/**
	 * Downloads the given asset index asynchronously.
	 * 
	 * @param version the version of the asset index to download
	 * @return the asset index
	 */
	CompletableFuture<AssetIndex> downloadAssetIndex(String version);

}
