package org.to2mbn.jmccc.mcdownloader.provider;

import java.net.URI;
import org.to2mbn.jmccc.mcdownloader.util.URIUtils;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Version;

abstract public class DefaultLayoutProvider extends URIDownloadProvider {

	@Deprecated
	@Override
	public URI getLibrary(Library library) {
		String baseurl = library.getCustomUrl();
		if (baseurl == null) {
			baseurl = getLibraryBaseURL();
		}
		String url = baseurl + library.getPath();
		if (library.getChecksums() != null) {
			url += ".pack.xz";
		}
		return URIUtils.toURI(url);
	}

	@Deprecated
	@Override
	public URI getGameJar(Version version) {
		return URIUtils.toURI(getVersionBaseURL() + version.getRoot() + "/" + version.getRoot() + ".jar");
	}

	@Deprecated
	@Override
	public URI getGameVersionJson(String version) {
		return URIUtils.toURI(getVersionBaseURL() + version + "/" + version + ".json");
	}

	@Deprecated
	@Override
	public URI getAssetIndex(Version version) {
		return URIUtils.toURI(getAssetIndexBaseURL() + version.getAssets() + ".json");
	}

	@Override
	public URI getVersionList() {
		return URIUtils.toURI(getVersionListURL());
	}

	@Override
	public URI getAsset(Asset asset) {
		return URIUtils.toURI(getAssetBaseURL() + asset.getPath());
	}

	@Deprecated
	abstract protected String getLibraryBaseURL();

	@Deprecated
	abstract protected String getVersionBaseURL();

	@Deprecated
	abstract protected String getAssetIndexBaseURL();

	abstract protected String getVersionListURL();

	abstract protected String getAssetBaseURL();

}
