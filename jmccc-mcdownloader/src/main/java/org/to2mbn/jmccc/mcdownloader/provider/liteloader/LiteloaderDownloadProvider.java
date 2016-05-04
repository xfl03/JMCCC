package org.to2mbn.jmccc.mcdownloader.provider.liteloader;

import java.io.IOException;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.to2mbn.jmccc.mcdownloader.download.FileDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.MemoryDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.ResultProcessor;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadContext;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.mcdownloader.provider.AbstractMinecraftDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.ExtendedDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.M2RepositorySupport;
import org.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.ToJsonResultProcessor;
import org.to2mbn.jmccc.mcdownloader.provider.VersionJsonWriteProcessor;
import org.to2mbn.jmccc.mcdownloader.util.VersionComparator;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.util.IOUtils;
import org.to2mbn.jmccc.version.Library;

public class LiteloaderDownloadProvider extends AbstractMinecraftDownloadProvider implements ExtendedDownloadProvider {

	private final VersionComparator versionComparator = new VersionComparator();

	private MinecraftDownloadProvider upstreamProvider;

	public CombinedDownloadTask<LiteloaderVersionList> liteloaderVersionList() {
		return CombinedDownloadTask.single(new MemoryDownloadTask(liteloaderVersionListUrl()).andThen(new ToJsonResultProcessor()).andThen(new ResultProcessor<JSONObject, LiteloaderVersionList>() {

			@Override
			public LiteloaderVersionList process(JSONObject json) throws Exception {
				return LiteloaderVersionList.fromJson(json);
			}
		}).cacheable());
	}

	@Override
	public CombinedDownloadTask<String> gameVersionJson(final MinecraftDirectory mcdir, String version) {
		final ResolvedLiteloaderVersion liteloaderInfo = ResolvedLiteloaderVersion.resolve(version);
		if (liteloaderInfo == null) {
			return null;
		}

		return upstreamProvider.gameVersionJson(mcdir, liteloaderInfo.getSuperVersion()).andThenDownload(new ResultProcessor<String, CombinedDownloadTask<LiteloaderVersion>>() {

			@Override
			public CombinedDownloadTask<LiteloaderVersion> process(final String resolvedSuperversion) throws Exception {
				return liteloaderVersionList().andThen(new ResultProcessor<LiteloaderVersionList, LiteloaderVersion>() {

					@Override
					public LiteloaderVersion process(LiteloaderVersionList versionList) throws Exception {
						String mcversion = liteloaderInfo.getMinecraftVersion();
						LiteloaderVersion genericLiteloader = versionList.getLatest(mcversion);
						if (genericLiteloader == null) {
							genericLiteloader = versionList.getSnapshot(mcversion);
						}

						if (genericLiteloader == null) {
							throw new IllegalArgumentException("Liteloader version not found: " + liteloaderInfo);
						}
						return genericLiteloader.customize(resolvedSuperversion);
					}
				});
			}
		}).andThenDownload(new ResultProcessor<LiteloaderVersion, CombinedDownloadTask<String>>() {

			@Override
			public CombinedDownloadTask<String> process(final LiteloaderVersion liteloader) throws Exception {
				if (liteloader.getLiteloaderVersion().endsWith("-SNAPSHOT")) {
					return CombinedDownloadTask.any(fetchVersionJsonFromGithub(liteloader.getMinecraftVersion()).andThen(new VersionJsonWriteProcessor(mcdir)));
				} else {
					return createLiteloaderVersionTask(mcdir, liteloader);
				}
			}
		});
	}

	@Override
	public CombinedDownloadTask<Void> library(final MinecraftDirectory mcdir, final Library library) {
		final String groupId = library.getGroupId();
		final String artifactId = library.getArtifactId();
		final String version = library.getVersion();

		if ("com.mumfrey".equals(groupId) && "liteloader".equals(artifactId)) {
			if (library.isSnapshotArtifact()) {
				return liteloaderVersionList().andThenDownload(new ResultProcessor<LiteloaderVersionList, CombinedDownloadTask<Void>>() {

					@Override
					public CombinedDownloadTask<Void> process(LiteloaderVersionList versionList) throws Exception {
						LiteloaderVersion liteloader = versionList.getSnapshot(version.substring(0, version.length() - "-SNAPSHOT".length()));
						if (liteloader != null) {
							final String repo = liteloader.getRepoUrl();
							if (repo != null) {
								return M2RepositorySupport.snapshotPostfix(groupId, artifactId, version, repo)
										.andThenDownload(new ResultProcessor<String, CombinedDownloadTask<Void>>() {

											@Override
											public CombinedDownloadTask<Void> process(String postfix) throws Exception {
												return CombinedDownloadTask.single(new FileDownloadTask(repo + new Library(groupId, artifactId, version, "release", library.getType()).getPath(postfix), mcdir.getLibrary(library)).cacheable());
											}
										});
							}
						}
						return upstreamProvider.library(mcdir, library);
					}
				});
			}
		}
		return null;
	}

	@Override
	public void setUpstreamProvider(MinecraftDownloadProvider upstreamProvider) {
		this.upstreamProvider = upstreamProvider;
	}

	private CombinedDownloadTask<String> createLiteloaderVersionTask(final MinecraftDirectory mcdir, final LiteloaderVersion liteloader) {
		return new CombinedDownloadTask<String>() {

			@Override
			public void execute(CombinedDownloadContext<String> context) throws Exception {
				context.done(new VersionJsonWriteProcessor(mcdir).process(createLiteloaderVersion(mcdir, liteloader)));
			}
		};
	}

	protected JSONObject createLiteloaderVersion(MinecraftDirectory mcdir, LiteloaderVersion liteloader) throws IOException {
		String superVersion = liteloader.getSuperVersion();
		String minecraftVersion = liteloader.getMinecraftVersion();
		String repoUrl = liteloader.getRepoUrl();
		String tweakClass = liteloader.getTweakClass();
		Set<JSONObject> liteloaderLibraries = liteloader.getLibraries();

		JSONObject versionjson = IOUtils.toJson(mcdir.getVersionJson(superVersion));

		String version = String.format("%s-LiteLoader%s", superVersion, minecraftVersion);
		String minecraftArguments = String.format("%s --tweakClass %s", versionjson.getString("minecraftArguments"),
				tweakClass == null ? "com.mumfrey.liteloader.launch.LiteLoaderTweaker" : tweakClass);
		JSONArray libraries = new JSONArray();
		JSONObject liteloaderLibrary = new JSONObject();
		liteloaderLibrary.put("name", String.format("com.mumfrey:liteloader:%s", minecraftVersion));
		liteloaderLibrary.put("url", repoUrl == null ? "http://dl.liteloader.com/versions/" : repoUrl);
		libraries.put(liteloaderLibrary);
		if (liteloaderLibraries != null) {
			String launchwrapperPrefix = launchwrapperName() + ":";
			for (JSONObject library : liteloaderLibraries) {
				String name = library.optString("name", null);
				if (name != null) {
					String leastLaunchwrapperVersion = leastLaunchwrapperVersion();
					if (leastLaunchwrapperVersion != null && name.startsWith(launchwrapperPrefix)) {
						String actualVersion = name.substring(launchwrapperPrefix.length());
						if (versionComparator.compare(actualVersion, leastLaunchwrapperVersion) < -1) {
							library.put("name", launchwrapperPrefix + leastLaunchwrapperVersion);
						}
					}
				}

				libraries.put(library);
			}
		}

		versionjson.put("inheritsFrom", superVersion);
		versionjson.put("minecraftArguments", minecraftArguments);
		versionjson.put("mainClass", "net.minecraft.launchwrapper.Launch");
		versionjson.put("id", version);
		versionjson.put("libraries", libraries);
		versionjson.remove("downloads");
		versionjson.remove("assets");
		versionjson.remove("assetIndex");
		return versionjson;
	}

	protected String liteloaderVersionListUrl() {
		return "http://dl.liteloader.com/versions/versions.json";
	}

	protected String githubVersionJsonUrl(String version) {
		return "https://raw.githubusercontent.com/Mumfrey/LiteLoaderInstaller/" + version + "/src/main/resources/install_profile.json";
	}

	protected String leastLaunchwrapperVersion() {
		return "1.7";
	}

	protected String launchwrapperName() {
		return "net.minecraft:launchwrapper";
	}

	private CombinedDownloadTask<JSONObject> fetchVersionJsonFromGithub(String version) {
		return CombinedDownloadTask.single(new MemoryDownloadTask(githubVersionJsonUrl(version)).cacheable())
				.andThen(new ToJsonResultProcessor());
	}

}
