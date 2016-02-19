package org.to2mbn.jmccc.mcdownloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.to2mbn.jmccc.mcdownloader.download.combine.AbstractCombinedDownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadContext;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Asset;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.jmccc.version.Versions;

public class IncrementallyDownloadTask extends CombinedDownloadTask<Version> {

	private MinecraftDirectory mcdir;
	private String version;
	private MinecraftDownloadProvider downloadProvider;

	private Set<String> handledVersions = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	private String resolvedVersion;

	public IncrementallyDownloadTask(MinecraftDownloadProvider downloadProvider, MinecraftDirectory mcdir, String version) {
		Objects.requireNonNull(mcdir);
		Objects.requireNonNull(version);
		Objects.requireNonNull(downloadProvider);
		this.mcdir = mcdir;
		this.version = version;
		this.downloadProvider = downloadProvider;
	}

	@Override
	public void execute(final CombinedDownloadContext<Version> context) throws Exception {
		handledVersions.clear();
		resolvedVersion = null;

		handleVersionJson(version, context, new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				if (resolvedVersion == null) {
					resolvedVersion = version;
				}

				final Version ver = Versions.resolveVersion(mcdir, resolvedVersion);
				if (!mcdir.getVersionJar(ver.getRoot()).exists()) {
					context.submit(downloadProvider.gameJar(mcdir, ver), null, true);
				}
				for (Library library : ver.getMissingLibraries(mcdir)) {
					context.submit(downloadProvider.library(mcdir, library), null, true);
				}
				if (mcdir.getAssetIndex(ver.getAssets()).exists()) {
					downloadAssets(context, Versions.resolveAssets(mcdir, ver.getAssets()));
				} else {
					context.submit(downloadProvider.assetsIndex(mcdir, ver), new AbstractCombinedDownloadCallback<Set<Asset>>() {

						@Override
						public void done(final Set<Asset> result) {
							try {
								context.submit(new Callable<Void>() {

									@Override
									public Void call() throws Exception {
										downloadAssets(context, result);
										return null;
									}
								}, null, true);
							} catch (InterruptedException e) {
								context.cancelled();
							}
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

	private void handleVersionJson(final String currentVersion, final CombinedDownloadContext<Version> context, final Callable<Void> callback) throws Exception {
		if (mcdir.getVersionJson(currentVersion).exists()) {
			JSONObject versionjson = readJson(mcdir.getVersionJson(currentVersion));
			String inheritsFrom = versionjson.optString("inheritsFrom", null);
			handledVersions.add(currentVersion);
			if (inheritsFrom == null) {
				// end node
				callback.call();
			} else {
				// intermediate node
				if (handledVersions.contains(inheritsFrom)) {
					throw new IllegalStateException("loop inherits from: " + currentVersion + " to " + inheritsFrom);
				}
				handleVersionJson(inheritsFrom, context, callback);
			}
		} else {
			context.submit(downloadProvider.gameVersionJson(mcdir, currentVersion), new AbstractCombinedDownloadCallback<String>() {

				@Override
				public void done(final String currentResolvedVersion) {
					try {
						context.submit(new Callable<Void>() {

							@Override
							public Void call() throws Exception {
								if (version.equals(currentVersion)) {
									resolvedVersion = currentResolvedVersion;
								}

								handleVersionJson(currentResolvedVersion, context, callback);
								return null;
							}
						}, null, true);
					} catch (InterruptedException e) {
						context.cancelled();
					}
				}

			}, true);
		}
	}

	private void downloadAssets(final CombinedDownloadContext<Version> context, Set<Asset> assets) throws NoSuchAlgorithmException, IOException, InterruptedException {
		Map<String, Asset> hashMapping = new HashMap<>();
		for (Asset asset : assets) {
			// put the assets into a map
			// to remove the elements which has the same hash
			hashMapping.put(asset.getHash(), asset);
		}
		for (final Asset asset : hashMapping.values()) {
			context.submit(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					if (!asset.isValid(mcdir)) {
						context.submit(downloadProvider.asset(mcdir, asset), null, false);
					}
					return null;
				}
			}, null, false);
		}
	}

	private JSONObject readJson(File file) throws IOException, JSONException {
		try (Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), "UTF-8")) {
			return new JSONObject(new JSONTokener(reader));
		}
	}

}
