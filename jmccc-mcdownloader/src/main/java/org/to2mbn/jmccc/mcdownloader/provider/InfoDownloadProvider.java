package org.to2mbn.jmccc.mcdownloader.provider;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.to2mbn.jmccc.mcdownloader.RemoteVersion;
import org.to2mbn.jmccc.mcdownloader.RemoteVersionList;
import org.to2mbn.jmccc.mcdownloader.download.FileDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.ResultProcessor;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.util.ChecksumUtils;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.DownloadInfo;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.LibraryInfo;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.jmccc.version.Versions;

public class InfoDownloadProvider extends AbstractMinecraftDownloadProvider implements ExtendedDownloadProvider {

	private MinecraftDownloadProvider upstreamProvider;

	@Override
	public CombinedDownloadTask<Set<Asset>> assetsIndex(final MinecraftDirectory mcdir, final Version version) {
		CombinedDownloadTask<Void> task = download(version.getAssetIndexDownloadInfo(), mcdir.getAssetIndex(version));
		if (task != null) {
			return task.andThen(new ResultProcessor<Void, Set<Asset>>() {

				@Override
				public Set<Asset> process(Void arg) throws Exception {
					return Versions.resolveAssets(mcdir, version);
				}
			});
		} else {
			return null;
		}
	}

	@Override
	public CombinedDownloadTask<Void> gameJar(MinecraftDirectory mcdir, Version version) {
		Map<String, DownloadInfo> downloads = version.getDownloads();
		if (downloads != null) {
			return download(downloads.get("client"), mcdir.getVersionJar(version));
		}
		return null;
	}

	@Override
	public CombinedDownloadTask<Void> library(MinecraftDirectory mcdir, Library library) {
		LibraryInfo info = library.getDownloadInfo();
		if (info != null) {
			return download(info, mcdir.getLibrary(library));
		}
		return null;
	}

	@Override
	public CombinedDownloadTask<String> gameVersionJson(final MinecraftDirectory mcdir, final String version) {
		if (upstreamProvider == null) {
			return null;
		} else {
			return upstreamProvider.versionList().andThenDownload(new ResultProcessor<RemoteVersionList, CombinedDownloadTask<String>>() {

				@Override
				public CombinedDownloadTask<String> process(RemoteVersionList result) throws Exception {
					final RemoteVersion remoteVersion = result.getVersions().get(version);
					if (remoteVersion != null && remoteVersion.getUrl() != null) {
						return CombinedDownloadTask.single(new FileDownloadTask(remoteVersion.getUrl(), mcdir.getVersionJson(remoteVersion.getVersion())).cacheable())
								.andThen(new ResultProcessor<Void, String>() {

							@Override
							public String process(Void arg) throws Exception {
								return remoteVersion.getVersion();
							}
						});
					}

					return upstreamProvider.gameVersionJson(mcdir, version);
				}
			});
		}
	}

	@Override
	public void setUpstreamProvider(MinecraftDownloadProvider upstreamProvider) {
		this.upstreamProvider = upstreamProvider;
	}

	private CombinedDownloadTask<Void> download(final DownloadInfo info, final File target) {
		if (info == null || info.getUrl() == null) {
			return null;
		}
		return CombinedDownloadTask.single(new FileDownloadTask(info.getUrl(), target).andThen(new ResultProcessor<Void, Void>() {

			@Override
			public Void process(Void arg) throws Exception {
				if (!ChecksumUtils.verify(target, info.getChecksum(), "SHA-1", info.getSize())) {
					throw new IOException("checksums mismatch");
				}
				return null;
			}
		}));
	}

}
