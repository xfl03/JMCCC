package org.to2mbn.jmccc.mcdownloader.provider;

import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.ResultProcessor;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Version;

import java.net.URI;

abstract public class DefaultLayoutProvider extends URIDownloadProvider {

    @Override
    public CombinedDownloadTask<Void> library(final MinecraftDirectory mcdir, final Library library) {
        if (library.isSnapshotArtifact()) {
            final String repo = getLibraryRepo(library);
            if (repo != null) {
                return MavenRepositories.snapshotPostfix(library.getGroupId(), library.getArtifactId(), library.getVersion(), repo)
                        .andThenDownload(new ResultProcessor<String, CombinedDownloadTask<Void>>() {

                            @Override
                            public CombinedDownloadTask<Void> process(String postfix) throws Exception {
                                String url = repo + library.getPath(postfix);
                                if (library.getChecksums() != null) {
                                    url += ".pack.xz";
                                }
                                return DefaultLayoutProvider.this.library(mcdir, library, URI.create(url));
                            }
                        });
            }
        }
        return super.library(mcdir, library);
    }

    @Override
    public URI[] getLibrary(Library library) {
        String url = getLibraryRepo(library);
        if (url == null) return null;
        url += library.getPath();
        if (library.getChecksums() != null) {
            return new URI[]{URI.create(url), URI.create(url + ".pack.xz")};
        } else {
            return new URI[]{URI.create(url)};
        }
    }

    private String getLibraryRepo(Library library) {
        String customizedUrl = library.getCustomizedUrl();
        return customizedUrl == null ? getLibraryBaseURL() : customizedUrl;
    }

    @Override
    public URI getGameJar(Version version) {
        String url = getVersionBaseURL();
        if (url == null) return null;
        return URI.create(url + version.getRoot() + "/" + version.getRoot() + ".jar");
    }

    @Override
    public URI getGameVersionJson(String version) {
        String url = getVersionBaseURL();
        if (url == null) return null;
        return URI.create(url + version + "/" + version + ".json");
    }

    @Override
    public URI getAssetIndex(Version version) {
        String url = getAssetIndexBaseURL();
        if (url == null) return null;
        return URI.create(url + version.getAssets() + ".json");
    }

    @Override
    public URI getVersionList() {
        String url = getVersionListURL();
        if (url == null) return null;
        return URI.create(url);
    }

    @Override
    public URI getAsset(Asset asset) {
        String url = getAssetBaseURL();
        if (url == null) return null;
        return URI.create(url + asset.getPath());
    }

    @Deprecated
    protected String getVersionBaseURL() {
        return null;
    }

    @Deprecated
    protected String getAssetIndexBaseURL() {
        return null;
    }

    protected String getLibraryBaseURL() {
        return null;
    }

    abstract protected String getVersionListURL();

    abstract protected String getAssetBaseURL();

}
