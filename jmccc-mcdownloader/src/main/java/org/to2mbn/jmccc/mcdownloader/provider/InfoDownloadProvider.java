package org.to2mbn.jmccc.mcdownloader.provider;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import org.to2mbn.jmccc.mcdownloader.RemoteVersionList;
import org.to2mbn.jmccc.mcdownloader.download.FileDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.ResultProcessor;
import org.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloadTask;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.util.ChecksumUtils;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.DownloadInfo;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.LibraryInfo;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.jmccc.version.Versions;

public class InfoDownloadProvider implements MinecraftDownloadProvider {

	@Override
	public MultipleDownloadTask<Set<Asset>> assetsIndex(final MinecraftDirectory mcdir, final Version version) {
		MultipleDownloadTask<Object> task = download(version.getAssetIndexDownloadInfo(), mcdir.getAssetIndex(version.getAssets()));
		if (task != null) {
			return task.andThen(new ResultProcessor<Object, Set<Asset>>() {

				@Override
				public Set<Asset> process(Object arg) throws Exception {
					return Versions.resolveAssets(mcdir, version.getAssets());
				}
			});
		} else {
			return null;
		}
	}

	@Override
	public MultipleDownloadTask<Object> gameJar(MinecraftDirectory mcdir, Version version) {
		Map<String, DownloadInfo> downloads = version.getDownloads();
		if (downloads != null) {
			return download(downloads.get("client"), mcdir.getVersionJar(version.getVersion()));
		}
		return null;
	}

	@Override
	public MultipleDownloadTask<Object> library(MinecraftDirectory mcdir, Library library) {
		LibraryInfo info = library.getDownloadInfo();
		if (info != null) {
			return download(info, new File(mcdir.getLibraries(), info.getPath()));
		}
		return null;
	}

	@Override
	public MultipleDownloadTask<RemoteVersionList> versionList() {
		return null;
	}

	@Override
	public MultipleDownloadTask<Object> gameVersionJson(MinecraftDirectory mcdir, String version) {
		return null;
	}

	@Override
	public MultipleDownloadTask<Object> asset(MinecraftDirectory mcdir, Asset asset) {
		return null;
	}

	private MultipleDownloadTask<Object> download(final DownloadInfo info, final File target) {
		if (info == null || info.getUrl() == null) {
			return null;
		}
		try {
			return MultipleDownloadTask.simple(new FileDownloadTask(new URI(info.getUrl()), target).andThen(new ResultProcessor<Object, Object>() {

				@Override
				public Object process(Object arg) throws Exception {
					if (!ChecksumUtils.verifyChecksum(target, info.getChecksum(), "SHA-1", info.getSize())) {
						throw new IOException("checksums mismatch");
					}
					return null;
				}
			}));
		} catch (URISyntaxException e) {
			// ignore
			e.printStackTrace();
			return null;
		}
	}

}
