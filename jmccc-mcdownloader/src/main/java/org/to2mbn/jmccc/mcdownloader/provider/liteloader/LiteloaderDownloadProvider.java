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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.to2mbn.jmccc.mcdownloader.download.MemoryDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.ResultProcessor;
import org.to2mbn.jmccc.mcdownloader.download.combine.AbstractCombinedDownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadContext;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.mcdownloader.provider.AbstractMinecraftDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.ExtendedDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.util.FileUtils;

public class LiteloaderDownloadProvider extends AbstractMinecraftDownloadProvider implements ExtendedDownloadProvider {

	private static final Pattern LITELOADER_VERSION_PATTERN = Pattern.compile("^([\\w\\.\\-]+)-[lL]ite[lL]oader\\1+$");
	
	private MinecraftDownloadProvider upstreamProvider;

	public CombinedDownloadTask<LiteloaderVersionList> liteloaderVersionList() {
		try {
			return CombinedDownloadTask.single(new MemoryDownloadTask(new URI("http://dl.liteloader.com/versions/versions.json")).andThen(new ResultProcessor<byte[], LiteloaderVersionList>() {

				@Override
				public LiteloaderVersionList process(byte[] arg) throws Exception {
					return LiteloaderVersionList.fromJson(new JSONObject(new String(arg, "UTF-8")));
				}
			}));
		} catch (URISyntaxException e) {
			throw new IllegalStateException("unable to convert to URI", e);
		}
	}

	@Override
	public CombinedDownloadTask<String> gameVersionJson(final MinecraftDirectory mcdir, final String version) {
		Matcher matcher=LITELOADER_VERSION_PATTERN.matcher(version);
		if (!matcher.matches()) {
			return null;
		}
		final String mcversion = matcher.group(1);
		return new CombinedDownloadTask<String>() {

			@Override
			public void execute(final CombinedDownloadContext<String> context) throws Exception {
				context.submit(liteloaderVersionList(), new AbstractCombinedDownloadCallback<LiteloaderVersionList>() {

					@Override
					public void done(final LiteloaderVersionList versionList) {
						try {
							context.submit(new Callable<Void>() {

								@Override
								public Void call() throws Exception {
									final LiteloaderVersion liteloaderVersion = versionList.getLatestArtefact(mcversion);
									context.submit(upstreamProvider.gameVersionJson(mcdir, liteloaderVersion.getMinecraftVersion()), new AbstractCombinedDownloadCallback<String>() {

										@Override
										public void done(String result) {
											try {
												context.submit(new Callable<Void>() {

													@Override
													public Void call() throws Exception {
														context.done(createLiteloaderVersion(mcdir, liteloaderVersion));
														return null;
													}
												}, null, true);
											} catch (InterruptedException e) {
												Thread.currentThread().interrupt();
											}
										}

									}, true);
									return null;
								}
							}, null, true);
						} catch (InterruptedException e) {
							context.cancelled();
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

	private String createLiteloaderVersion(MinecraftDirectory mcdir, LiteloaderVersion liteloader) throws IOException {
		String mcversion = liteloader.getMinecraftVersion();
		JSONObject versionjson;
		try (Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(mcdir.getVersionJson(mcversion))), "UTF-8")) {
			versionjson = new JSONObject(new JSONTokener(reader));
		}

		String version = String.format("%s-LiteLoader%s", mcversion, mcversion);
		String minecraftArguments = String.format("%s --tweakClass %s", versionjson.getString("minecraftArguments"), liteloader.getTweakClass());
		JSONArray libraries = new JSONArray();
		JSONObject liteloaderLibrary = new JSONObject();
		liteloaderLibrary.put("name", String.format("com.mumfrey:liteloader:%s", mcversion));
		liteloaderLibrary.put("url", "http://dl.liteloader.com/versions/");
		libraries.put(liteloaderLibrary);
		for (JSONObject library : liteloader.getLibraries()) {
			libraries.put(library);
		}

		versionjson.put("inheritsFrom", mcversion);
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

}
