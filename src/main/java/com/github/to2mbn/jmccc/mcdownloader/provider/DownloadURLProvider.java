package com.github.to2mbn.jmccc.mcdownloader.provider;

import com.github.to2mbn.jmccc.mcdownloader.Asset;
import com.github.to2mbn.jmccc.version.Library;

public interface DownloadURLProvider {

	String getRemoteVersionList();

	String getVersionJson(String version);

	String getVersionJar(String version);

	String getLibrary(Library library);

	String getAsset(Asset asset);

	String getAssetsIndex(String version);

}
