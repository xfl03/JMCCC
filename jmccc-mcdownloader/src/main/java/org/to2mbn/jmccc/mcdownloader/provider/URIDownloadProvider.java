package org.to2mbn.jmccc.mcdownloader.provider;

import org.to2mbn.jmccc.internal.org.json.JSONObject;
import org.to2mbn.jmccc.mcdownloader.RemoteVersionList;
import org.to2mbn.jmccc.mcdownloader.download.cache.CacheNames;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.FileDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.MemoryDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.tasks.ResultProcessor;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.jmccc.version.parsing.Versions;

import java.io.IOException;
import java.net.URI;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

abstract public class URIDownloadProvider implements MinecraftDownloadProvider {

    private Map<String, LibraryDownloadHandler> libraryHandlers = new ConcurrentSkipListMap<>(new Comparator<String>() {

        @Override
        public int compare(String o1, String o2) {
            int result = o2.length() - o1.length();
            if (result == 0) {
                result = o1.compareTo(o2);
            }
            return result;
        }
    });

    public URIDownloadProvider() {
        registerLibraryDownloadHandler(".jar", new JarLibraryDownloadHandler());
        registerLibraryDownloadHandler(".pack", new PackLibraryDownloadHandler());
        registerLibraryDownloadHandler(".pack.xz", new XZPackLibraryDownloadHandler());
    }

    // @formatter:off
    protected URI[] getLibrary(Library library) {
        return null;
    }

    protected URI getGameJar(Version version) {
        return null;
    }

    protected URI getGameVersionJson(String version) {
        return null;
    }

    protected URI getAssetIndex(Version version) {
        return null;
    }

    protected URI getVersionList() {
        return null;
    }
    // @formatter:on

    protected URI getAsset(Asset asset) {
        return null;
    }

    @Override
    public CombinedDownloadTask<RemoteVersionList> versionList() {
        URI uri = getVersionList();
        if (uri == null) {
            return null;
        }
        return CombinedDownloadTask.single(
                new MemoryDownloadTask(uri)
                        .andThen(new JsonDecoder())
                        .andThen(new ResultProcessor<JSONObject, RemoteVersionList>() {

                            @Override
                            public RemoteVersionList process(JSONObject json) throws Exception {
                                return RemoteVersionList.fromJson(json);
                            }
                        })
                        .cacheable()
                        .cachePool(CacheNames.VERSION_LIST));
    }

    @Override
    public CombinedDownloadTask<Set<Asset>> assetsIndex(final MinecraftDirectory mcdir, final Version version) {
        URI uri = getAssetIndex(version);
        if (uri == null) {
            return null;
        }
        return CombinedDownloadTask.single(
                new FileDownloadTask(uri, mcdir.getAssetIndex(version.getAssets()))
                        .andThen(new ResultProcessor<Void, Set<Asset>>() {

                            @Override
                            public Set<Asset> process(Void arg) throws IOException {
                                return Versions.resolveAssets(mcdir, version);
                            }
                        })
                        .cachePool(CacheNames.ASSET_INDEX));
    }

    @Override
    public CombinedDownloadTask<Void> gameJar(MinecraftDirectory mcdir, Version version) {
        URI uri = getGameJar(version);
        if (uri == null) {
            return null;
        }
        return CombinedDownloadTask.single(
                new FileDownloadTask(uri, mcdir.getVersionJar(version))
                        .cachePool(CacheNames.GAME_JAR));
    }

    @Override
    public CombinedDownloadTask<String> gameVersionJson(MinecraftDirectory mcdir, String version) {
        URI uri = getGameVersionJson(version);
        if (uri == null) {
            return null;
        }
        return CombinedDownloadTask.single(
                        new FileDownloadTask(uri, mcdir.getVersionJson(version))
                                .cacheable()
                                .cachePool(CacheNames.VERSION_JSON))
                .andThenReturn(version);
    }

    @Override
    public CombinedDownloadTask<Void> library(MinecraftDirectory mcdir, Library library) {
        URI[] uris = getLibrary(library);
        if (uris == null || uris.length == 0) {
            return null;
        }
        return library(mcdir, library, uris);
    }

    public CombinedDownloadTask<Void> library(MinecraftDirectory mcdir, Library library, URI... uris) {
        @SuppressWarnings("unchecked")
        DownloadTask<Void>[] tasks = new DownloadTask[uris.length];
        for (int i = 0; i < uris.length; i++) {
            URI uri = uris[i];
            String path = uri.getPath();
            LibraryDownloadHandler handler = null;
            for (Entry<String, LibraryDownloadHandler> entry : libraryHandlers.entrySet()) {
                if (path.endsWith(entry.getKey())) {
                    handler = entry.getValue();
                    break;
                }
            }
            if (handler == null)
                throw new IllegalArgumentException("unable to resolve library download handler, path: " + path);
            tasks[i] = handler.createDownloadTask(mcdir.getLibrary(library), library, uri);
        }

        return CombinedDownloadTask.any(tasks)
                .cachePool(CacheNames.LIBRARY);
    }

    @Override
    public CombinedDownloadTask<Void> asset(MinecraftDirectory mcdir, Asset asset) {
        URI uri = getAsset(asset);
        if (uri == null) {
            return null;
        }
        return CombinedDownloadTask.single(
                new FileDownloadTask(uri, mcdir.getAsset(asset))
                        .cachePool(CacheNames.ASSET));
    }

    public void registerLibraryDownloadHandler(String postfix, LibraryDownloadHandler handler) {
        Objects.requireNonNull(postfix);
        Objects.requireNonNull(handler);
        libraryHandlers.put(postfix, handler);
    }

    public void unregisterLibraryDownloadHandler(String postfix) {
        libraryHandlers.remove(postfix);
    }
}
