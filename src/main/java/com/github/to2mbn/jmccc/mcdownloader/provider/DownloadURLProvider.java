package com.github.to2mbn.jmccc.mcdownloader.provider;

import java.net.URL;
import com.github.to2mbn.jmccc.mcdownloader.Asset;
import com.github.to2mbn.jmccc.version.Library;

public interface DownloadURLProvider {

	URL getLibrary(Library library);

	URL getGameJar(String version);

	URL getAssetIndex(String version);

	URL getVersionList();

	URL getAsset(Asset asset);

}
