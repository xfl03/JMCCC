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

	@Override
	public CombinedDownloadTask<Void> library(final MinecraftDirectory mcdir, final Library library) {
		if (library.isSnapshotArtifact()) {
			final String repo = getLibraryRepo(library);
			return M2RepositorySupport.snapshotPostfix(library.getGroupId(), library.getArtifactId(), library.getVersion(), repo)
					.andThenDownload(new ResultProcessor<String, CombinedDownloadTask<Void>>() {

						@Override
						public CombinedDownloadTask<Void> process(String postfix) throws Exception {
							String url = repo + library.getPath(postfix);
							if (library.getChecksums() != null) {
								url += ".pack.xz";
							}
							return DefaultLayoutProvider.this.library(mcdir, library, URIUtils.toURI(url));
						}
					});
		}
		return super.library(mcdir, library);
	}

	@Override
	public URI getLibrary(Library library) {
		String url = getLibraryRepo(library) + library.getPath();
		if (library.getChecksums() != null) {
			url += ".pack.xz";
		}
		return URIUtils.toURI(url);
	}

	private String getLibraryRepo(Library library) {
		String repo = library.getCustomizedUrl();
		if (repo == null) {
			repo = getLibraryBaseURL();
		}
		return repo;
	}

	@Override
	public URI getGameJar(Version version) {
		return URIUtils.toURI(getVersionBaseURL() + version.getRoot() + "/" + version.getRoot() + ".jar");
	}

	@Override
	public URI getGameVersionJson(String version) {
		return URIUtils.toURI(getVersionBaseURL() + version + "/" + version + ".json");
	}

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

	abstract protected String getLibraryBaseURL();
	abstract protected String getVersionBaseURL();
	abstract protected String getAssetIndexBaseURL();
	abstract protected String getVersionListURL();
	abstract protected String getAssetBaseURL();

}
