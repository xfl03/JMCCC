package com.github.to2mbn.jmccc.mcdownloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloadCallback;
import com.github.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloadContext;
import com.github.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloadTask;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;
import com.github.to2mbn.jmccc.version.Asset;
import com.github.to2mbn.jmccc.version.Library;
import com.github.to2mbn.jmccc.version.Version;
import com.github.to2mbn.jmccc.version.Versions;

public class IncrementallyDownloadTask implements MultipleDownloadTask<Version> {

	private MinecraftDirectory mcdir;
	private String version;
	private MinecraftDownloadFactory downloadFactory;
	private Set<String> handledVersions = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

	public IncrementallyDownloadTask(MinecraftDownloadFactory downloadFactory, MinecraftDirectory mcdir, String version) {
		Objects.requireNonNull(mcdir);
		Objects.requireNonNull(version);
		Objects.requireNonNull(downloadFactory);
		this.mcdir = mcdir;
		this.version = version;
		this.downloadFactory = downloadFactory;
	}

	@Override
	public void execute(final MultipleDownloadContext<Version> context) throws Exception {
		handleVersionJson(version, context, new Callable<Object>() {

			@Override
			public Object call() throws Exception {
				final Version ver = Versions.resolveVersion(mcdir, version);
				for (Library library : ver.getMissingLibraries(mcdir)) {
					context.submit(downloadFactory.library(mcdir, library), null, true);
				}
				if (mcdir.getAssetIndex(ver.getAssets()).exists()) {
					downloadAssets(context, Versions.resolveAssets(mcdir, ver.getAssets()));
				} else {
					context.submit(downloadFactory.assetsIndex(mcdir, ver.getAssets()), new DownloadCallback<Set<Asset>>() {

						@Override
						public void done(final Set<Asset> result) {
							try {
								context.submit(new Callable<Object>() {

									@Override
									public Object call() throws Exception {
										downloadAssets(context, result);
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
				context.awaitAllTasks(new Runnable() {

					@Override
					public void run() {
						context.done(ver);
					}
				});
				return null;
			}
		});
	}

	private void handleVersionJson(final String version, final MultipleDownloadContext<Version> context, final Callable<?> callback) throws Exception {
		if (mcdir.getVersionJson(version).exists()) {
			JSONObject versionjson = readJson(mcdir.getVersionJson(version));
			String inheritsFrom = versionjson.optString("inheritsFrom", null);
			handledVersions.add(version);
			if (inheritsFrom == null) {
				// end node
				callback.call();
				if (!mcdir.getVersionJar(version).exists()) {
					context.submit(downloadFactory.gameJar(mcdir, version), null, true);
				}
			} else {
				// intermediate node
				if (handledVersions.contains(inheritsFrom)) {
					throw new IllegalStateException("loop inherits from: " + version + " to " + inheritsFrom);
				}
				handleVersionJson(inheritsFrom, context, callback);
			}
		} else {
			context.submit(downloadFactory.gameVersionJson(mcdir, version), new DownloadCallback<Object>() {

				@Override
				public void done(Object result) {
					try {
						context.submit(new Callable<Object>() {

							@Override
							public Object call() throws Exception {
								handleVersionJson(version, context, callback);
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
	}

	private void downloadAssets(MultipleDownloadContext<Version> context, Set<Asset> assets) throws NoSuchAlgorithmException, IOException, InterruptedException {
		for (Asset asset : assets) {
			if (!asset.isValid(mcdir)) {
				context.submit(downloadFactory.asset(mcdir, asset), null, false);
			}
		}
	}

	private JSONObject readJson(File file) throws IOException, JSONException {
		try (Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), "UTF-8")) {
			return new JSONObject(new JSONTokener(reader));
		}
	}

}
