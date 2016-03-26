package org.to2mbn.jmccc.mcdownloader.provider;

import java.io.IOException;
import java.net.URI;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import org.json.JSONObject;
import org.to2mbn.jmccc.mcdownloader.RemoteVersionList;
import org.to2mbn.jmccc.mcdownloader.download.FileDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.MemoryDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.ResultProcessor;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.jmccc.version.Versions;

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

	@Deprecated
	protected URI getLibrary(Library library) {
		return null;
	}

	@Deprecated
	protected URI getGameJar(Version version) {
		return null;
	}

	@Deprecated
	protected URI getGameVersionJson(String version) {
		return null;
	}

	@Deprecated
	protected URI getAssetIndex(Version version) {
		return null;
	}

	protected URI getVersionList() {
		return null;
	}

	protected URI getAsset(Asset asset) {
		return null;
	}

	public URIDownloadProvider() {
		registerLibraryDownloadHandler(".jar", new JarLibraryDownloadHandler());
		registerLibraryDownloadHandler(".pack", new PackLibraryDownloadHandler());
		registerLibraryDownloadHandler(".pack.xz", new XZPackLibraryDownloadHandler());
	}

	@Override
	public CombinedDownloadTask<RemoteVersionList> versionList() {
		URI uri = getVersionList();
		if (uri == null) {
			return null;
		}
		return CombinedDownloadTask.single(new MemoryDownloadTask(uri).andThen(new ResultProcessor<byte[], RemoteVersionList>() {

			@Override
			public RemoteVersionList process(byte[] arg) throws Exception {
				return RemoteVersionList.fromJson(new JSONObject(new String(arg, "UTF-8")));
			}
		}).cacheable());
	}

	@Deprecated
	@Override
	public CombinedDownloadTask<Set<Asset>> assetsIndex(final MinecraftDirectory mcdir, final Version version) {
		URI uri = getAssetIndex(version);
		if (uri == null) {
			return null;
		}
		return CombinedDownloadTask.single(new FileDownloadTask(uri, mcdir.getAssetIndex(version.getAssets())).andThen(new ResultProcessor<Void, Set<Asset>>() {

			@Override
			public Set<Asset> process(Void arg) throws IOException {
				return Versions.resolveAssets(mcdir, version);
			}
		}));
	}

	@Deprecated
	@Override
	public CombinedDownloadTask<Void> gameJar(MinecraftDirectory mcdir, Version version) {
		URI uri = getGameJar(version);
		if (uri == null) {
			return null;
		}
		return CombinedDownloadTask.single(new FileDownloadTask(uri, mcdir.getVersionJar(version)));
	}

	@Deprecated
	@Override
	public CombinedDownloadTask<String> gameVersionJson(MinecraftDirectory mcdir, final String version) {
		URI uri = getGameVersionJson(version);
		if (uri == null) {
			return null;
		}
		return CombinedDownloadTask.single(new FileDownloadTask(uri, mcdir.getVersionJson(version)).cacheable()).andThen(new ResultProcessor<Void, String>() {

			@Override
			public String process(Void arg) throws Exception {
				return version;
			}
		});
	}

	@Deprecated
	@Override
	public CombinedDownloadTask<Void> library(MinecraftDirectory mcdir, Library library) {
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
		return CombinedDownloadTask.single(handler.createDownloadTask(mcdir.getLibrary(library), library, uri));
	}

	@Override
	public CombinedDownloadTask<Void> asset(MinecraftDirectory mcdir, Asset asset) {
		URI uri = getAsset(asset);
		if (uri == null) {
			return null;
		}
		return CombinedDownloadTask.single(new FileDownloadTask(uri, mcdir.getAsset(asset)));
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
