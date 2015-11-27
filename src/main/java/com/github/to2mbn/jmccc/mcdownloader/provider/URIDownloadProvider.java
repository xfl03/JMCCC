package com.github.to2mbn.jmccc.mcdownloader.provider;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;
import org.json.JSONObject;
import com.github.to2mbn.jmccc.mcdownloader.RemoteVersionList;
import com.github.to2mbn.jmccc.mcdownloader.download.FileDownloadTask;
import com.github.to2mbn.jmccc.mcdownloader.download.MemoryDownloadTask;
import com.github.to2mbn.jmccc.mcdownloader.download.ResultProcessor;
import com.github.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloadTask;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;
import com.github.to2mbn.jmccc.version.Asset;
import com.github.to2mbn.jmccc.version.Library;
import com.github.to2mbn.jmccc.version.Versions;

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

	abstract protected URI getLibrary(Library library);

	abstract protected URI getGameJar(String version);

	abstract protected URI getGameVersionJson(String version);

	abstract protected URI getAssetIndex(String version);

	abstract protected URI getVersionList();

	abstract protected URI getAsset(Asset asset);

	public URIDownloadProvider() {
		registerLibraryDownloadHandler(".jar", new JarLibraryDownloadHandler());
		registerLibraryDownloadHandler(".pack", new PackLibraryDownloadHandler());
		registerLibraryDownloadHandler(".pack.xz", new XZPackLibraryDownloadHandler());
	}

	@Override
	public MultipleDownloadTask<RemoteVersionList> versionList() {
		URI uri = getVersionList();
		if (uri == null) {
			return null;
		}
		return MultipleDownloadTask.simple(new MemoryDownloadTask(uri).andThen(new ResultProcessor<byte[], RemoteVersionList>() {

			@Override
			public RemoteVersionList process(byte[] arg) throws Exception {
				return RemoteVersionList.fromJson(new JSONObject(new String(arg, "UTF-8")));
			}
		}));
	}

	@Override
	public MultipleDownloadTask<Set<Asset>> assetsIndex(final MinecraftDirectory mcdir, final String version) {
		URI uri = getAssetIndex(version);
		if (uri == null) {
			return null;
		}
		return MultipleDownloadTask.simple(new FileDownloadTask(uri, mcdir.getAssetIndex(version)).andThen(new ResultProcessor<Object, Set<Asset>>() {

			@Override
			public Set<Asset> process(Object arg) throws IOException {
				return Versions.resolveAssets(mcdir, version);
			}
		}));
	}

	@Override
	public MultipleDownloadTask<Object> gameJar(MinecraftDirectory mcdir, String version) {
		URI uri = getGameJar(version);
		if (uri == null) {
			return null;
		}
		return MultipleDownloadTask.simple(new FileDownloadTask(uri, mcdir.getVersionJar(version)));
	}

	@Override
	public MultipleDownloadTask<Object> gameVersionJson(MinecraftDirectory mcdir, String version) {
		URI uri = getGameVersionJson(version);
		if (uri == null) {
			return null;
		}
		return MultipleDownloadTask.simple(new FileDownloadTask(uri, mcdir.getVersionJson(version)));
	}

	@Override
	public MultipleDownloadTask<Object> library(MinecraftDirectory mcdir, Library library) {
		URI uri = getLibrary(library);
		if (uri == null) {
			return null;
		}
		String path = uri.getPath();
		LibraryDownloadHandler handler = null;
		for (Entry<String, LibraryDownloadHandler> entry : libraryHandlers.entrySet()) {
			if (path.endsWith(entry.getKey())) {
				handler = entry.getValue();
				break;
			}
		}
		if (handler == null) {
			throw new IllegalArgumentException("unable to resolve library download handler, path: " + path);
		}
		return MultipleDownloadTask.simple(handler.createDownloadTask(new File(mcdir.getLibraries(), library.getPath()), library, uri));
	}

	@Override
	public MultipleDownloadTask<Object> asset(MinecraftDirectory mcdir, Asset asset) {
		URI uri = getAsset(asset);
		if (uri == null) {
			return null;
		}
		return MultipleDownloadTask.simple(new FileDownloadTask(uri, new File(mcdir.getAssetObjects(), asset.getPath())));
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
