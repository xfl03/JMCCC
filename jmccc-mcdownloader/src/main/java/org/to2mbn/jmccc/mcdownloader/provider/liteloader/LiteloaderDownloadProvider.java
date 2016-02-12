package org.to2mbn.jmccc.mcdownloader.provider.liteloader;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import org.json.JSONObject;
import org.to2mbn.jmccc.mcdownloader.RemoteVersionList;
import org.to2mbn.jmccc.mcdownloader.download.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.MemoryDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.ResultProcessor;
import org.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloadContext;
import org.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloadTask;
import org.to2mbn.jmccc.mcdownloader.provider.InstallProfileProcessor;
import org.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Version;

public class LiteloaderDownloadProvider implements MinecraftDownloadProvider {

	private static final Pattern LITELOADER_VERSION_PATTERN = Pattern.compile("^([\\w\\.\\-]+)-LiteLoader[\\w\\.\\-]+$");

	public DownloadTask<LiteloaderVersionList> liteloaderVersionList() {
		try {
			return new MemoryDownloadTask(new URI("http://dl.liteloader.com/versions/versions.json")).andThen(new ResultProcessor<byte[], LiteloaderVersionList>() {

				@Override
				public LiteloaderVersionList process(byte[] arg) throws Exception {
					return LiteloaderVersionList.fromJson(new JSONObject(new String(arg, "UTF-8")));
				}
			});
		} catch (URISyntaxException e) {
			throw new IllegalStateException("unable to convert to URI", e);
		}
	}

	@Override
	public MultipleDownloadTask<Object> gameVersionJson(final MinecraftDirectory mcdir, final String version) {
		if (!LITELOADER_VERSION_PATTERN.matcher(version).matches()) {
			return null;
		}
		final String mcversion = version.substring(0, version.indexOf("-LiteLoader"));
		return new MultipleDownloadTask<Object>() {

			@Override
			public void execute(final MultipleDownloadContext<Object> context) throws Exception {
				context.submit(liteloaderVersionList(), new DownloadCallback<LiteloaderVersionList>() {

					@Override
					public void done(final LiteloaderVersionList versionList) {
						try {
							context.submit(new Callable<Object>() {

								@Override
								public Object call() throws Exception {
									LiteloaderVersion liteloaderVersion = versionList.getLatestArtefact(mcversion);
									context.submit(new MemoryDownloadTask(new URI("http://dl.liteloader.com/redist/" + mcversion + "/liteloader-installer-" + liteloaderVersion.getLiteloaderVersion().replace('_', '-') + ".jar")).andThen(new InstallProfileProcessor(mcdir.getVersionJson(version))), null, true);
									context.awaitAllTasks(new Runnable() {

										@Override
										public void run() {
											context.done(null);
										}
									});
									return null;
								}
							}, null, true);
						} catch (InterruptedException e) {
							context.cancelled();
						}
					}

					@Override
					public void failed(Throwable e) {
					}

					@Override
					public void cancelled() {
					}

					@Override
					public void updateProgress(long done, long total) {
					}

					@Override
					public void retry(Throwable e, int current, int max) {
					}
				}, true);
			}
		};
	}

	@Override
	public MultipleDownloadTask<RemoteVersionList> versionList() {
		return null;
	}

	@Override
	public MultipleDownloadTask<Set<Asset>> assetsIndex(MinecraftDirectory mcdir, Version version) {
		return null;
	}

	@Override
	public MultipleDownloadTask<Object> gameJar(MinecraftDirectory mcdir, Version version) {
		return null;
	}

	@Override
	public MultipleDownloadTask<Object> library(MinecraftDirectory mcdir, Library library) {
		return null;
	}

	@Override
	public MultipleDownloadTask<Object> asset(MinecraftDirectory mcdir, Asset asset) {
		return null;
	}

}
