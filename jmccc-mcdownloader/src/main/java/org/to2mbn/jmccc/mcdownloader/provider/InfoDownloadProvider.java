package org.to2mbn.jmccc.mcdownloader.provider;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.to2mbn.jmccc.mcdownloader.RemoteVersion;
import org.to2mbn.jmccc.mcdownloader.RemoteVersionList;
import org.to2mbn.jmccc.mcdownloader.download.FileDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.ResultProcessor;
import org.to2mbn.jmccc.mcdownloader.download.combine.AbstractCombinedDownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadContext;
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
		}
		return new CombinedDownloadTask<String>() {

			@Override
			public void execute(final CombinedDownloadContext<String> context) throws Exception {
				context.submit(upstreamProvider.versionList(), new AbstractCombinedDownloadCallback<RemoteVersionList>() {

					@Override
					public void done(final RemoteVersionList result) {
						try {
							context.submit(new Callable<Void>() {

								@Override
								public Void call() throws Exception {
									final RemoteVersion remoteVersion = result.getVersions().get(version);
									if (remoteVersion != null) {
										String url = remoteVersion.getUrl();
										if (url != null) {
											context.submit(new FileDownloadTask(url, mcdir.getVersionJson(remoteVersion.getVersion())), null, true);
											context.awaitAllTasks(new Runnable() {

												@Override
												public void run() {
													context.done(remoteVersion.getVersion());
												}
											});
											return null;
										}
									}

									context.submit(upstreamProvider.gameVersionJson(mcdir, version), new AbstractCombinedDownloadCallback<String>() {

										@Override
										public void done(String result) {
											context.done(result);
										}

									}, true);
									return null;
								}
							}, null, true);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}

				}, true);
			}
		};
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
