package org.to2mbn.jmccc.mcdownloader.provider;

import java.net.URI;
import org.to2mbn.jmccc.mcdownloader.download.ResultProcessor;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.mcdownloader.util.URIUtils;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Version;

abstract public class DefaultLayoutProvider extends URIDownloadProvider {

	@Deprecated
	@Override
	public CombinedDownloadTask<Void> library(final MinecraftDirectory mcdir, final Library library) {
		if (M2RepositorySupport.isSnapshotVersion(library.getVersion())) {
			return M2RepositorySupport.snapshotPostfix(library.getDomain(), library.getName(), library.getVersion(), getLibraryRepo(library))
					.andThenDownload(new ResultProcessor<String, CombinedDownloadTask<Void>>() {

						@Override
						public CombinedDownloadTask<Void> process(String timestamp) throws Exception {
							String url = getLibraryRepo(library)
									+ library.getDomain().replace('.', '/') + '/'
									+ library.getName() + '/'
									+ library.getVersion() + '/'
									+ library.getName() + '-' + M2RepositorySupport.fillInTimestamp(library.getVersion(), timestamp) + ".jar";
							if (library.getChecksums() != null) {
								url += ".pack.xz";
							}
							return DefaultLayoutProvider.this.library(mcdir, library, URIUtils.toURI(url));
						}
					});
		}
		return super.library(mcdir, library);
	}

	@Deprecated
	@Override
	public URI getLibrary(Library library) {
		String url = getLibraryRepo(library) + library.getPath();
		if (library.getChecksums() != null) {
			url += ".pack.xz";
		}
		return URIUtils.toURI(url);
	}

	@Deprecated
	private String getLibraryRepo(Library library) {
		String repo = library.getCustomUrl();
		if (repo == null) {
			repo = getLibraryBaseURL();
		}
		return repo;
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
