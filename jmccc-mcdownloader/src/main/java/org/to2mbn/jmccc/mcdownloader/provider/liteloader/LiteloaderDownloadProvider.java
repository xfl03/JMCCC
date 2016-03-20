package org.to2mbn.jmccc.mcdownloader.provider.liteloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.Callable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.to2mbn.jmccc.mcdownloader.download.MemoryDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.ResultProcessor;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.mcdownloader.provider.AbstractMinecraftDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.ExtendedDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.util.FileUtils;

public class LiteloaderDownloadProvider extends AbstractMinecraftDownloadProvider implements ExtendedDownloadProvider {

	private MinecraftDownloadProvider upstreamProvider;

	public CombinedDownloadTask<LiteloaderVersionList> liteloaderVersionList() {
		return CombinedDownloadTask.single(new MemoryDownloadTask(liteloaderVersionListUrl()).andThen(new ResultProcessor<byte[], LiteloaderVersionList>() {

			@Override
			public LiteloaderVersionList process(byte[] arg) throws Exception {
				return LiteloaderVersionList.fromJson(new JSONObject(new String(arg, "UTF-8")));
			}
		}));
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
						LiteloaderVersion genericLiteloader = versionList.getLatestArtefact(liteloaderInfo.getMinecraftVersion());
						if (genericLiteloader == null) {
							throw new IllegalArgumentException("Liteloader version not found: " + liteloaderInfo);
						}
						return genericLiteloader.customize(resolvedSuperversion);
					}
				});
			}
		}).andThenCall(new ResultProcessor<LiteloaderVersion, Callable<String>>() {

			@Override
			public Callable<String> process(final LiteloaderVersion liteloader) throws Exception {
				return new Callable<String>() {

					@Override
					public String call() throws Exception {
						return createLiteloaderVersion(mcdir, liteloader);
					}
				};
			}
		});
	}

	@Override
	public void setUpstreamProvider(MinecraftDownloadProvider upstreamProvider) {
		this.upstreamProvider = upstreamProvider;
	}

	protected String createLiteloaderVersion(MinecraftDirectory mcdir, LiteloaderVersion liteloader) throws IOException {
		String superVersion = liteloader.getSuperVersion();
		String minecraftVersion = liteloader.getMinecraftVersion();
		JSONObject versionjson;
		try (Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(mcdir.getVersionJson(superVersion))), "UTF-8")) {
			versionjson = new JSONObject(new JSONTokener(reader));
		}

		String version = String.format("%s-LiteLoader%s", superVersion, minecraftVersion);
		String minecraftArguments = String.format("%s --tweakClass %s", versionjson.getString("minecraftArguments"), liteloader.getTweakClass());
		JSONArray libraries = new JSONArray();
		JSONObject liteloaderLibrary = new JSONObject();
		liteloaderLibrary.put("name", String.format("com.mumfrey:liteloader:%s", minecraftVersion));
		liteloaderLibrary.put("url", "http://dl.liteloader.com/versions/");
		libraries.put(liteloaderLibrary);
		for (JSONObject library : liteloader.getLibraries()) {
			libraries.put(library);
		}

		versionjson.put("inheritsFrom", superVersion);
		versionjson.put("minecraftArguments", minecraftArguments);
		versionjson.put("mainClass", "net.minecraft.launchwrapper.Launch");
		versionjson.put("id", version);
		versionjson.put("libraries", libraries);
		versionjson.remove("downloads");
		versionjson.remove("assets");
		versionjson.remove("assetIndex");

		File target = mcdir.getVersionJson(version);
		FileUtils.prepareWrite(target);
		try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(target)), "UTF-8")) {
			writer.write(versionjson.toString(4));
		}

		return version;
	}

	protected String liteloaderVersionListUrl() {
		return "http://dl.liteloader.com/versions/versions.json";
	}

}
