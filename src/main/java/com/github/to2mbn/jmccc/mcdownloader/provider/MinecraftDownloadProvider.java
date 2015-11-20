package com.github.to2mbn.jmccc.mcdownloader.provider;

import java.net.URI;
import com.github.to2mbn.jmccc.version.Asset;
import com.github.to2mbn.jmccc.version.Library;

public interface MinecraftDownloadProvider {

	URI getLibrary(Library library);

	URI getGameJar(String version);

	URI getGameVersionJson(String version);

	URI getAssetIndex(String version);

	URI getVersionList();

	URI getAsset(Asset asset);

}
