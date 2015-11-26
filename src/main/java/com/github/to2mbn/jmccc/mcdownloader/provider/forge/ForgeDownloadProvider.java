package com.github.to2mbn.jmccc.mcdownloader.provider.forge;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.json.JSONObject;
import org.json.JSONTokener;
import com.github.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import com.github.to2mbn.jmccc.mcdownloader.download.MemoryDownloadTask;
import com.github.to2mbn.jmccc.mcdownloader.download.ResultProcessor;
import com.github.to2mbn.jmccc.mcdownloader.provider.URIDownloadProvider;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;
import com.github.to2mbn.jmccc.version.Asset;
import com.github.to2mbn.jmccc.version.Library;

public class ForgeDownloadProvider extends URIDownloadProvider {

	private static final Pattern FORGE_VERSION_PATTERN = Pattern.compile("^([\\w\\.\\-]+)-forge\\1-[\\w\\.\\-]+$");

	public DownloadTask<ForgeVersionList> forgeVersionList() {
		try {
			return new MemoryDownloadTask(new URI("http://files.minecraftforge.net/maven/net/minecraftforge/forge/json")).andThen(new ResultProcessor<byte[], ForgeVersionList>() {

				@Override
				public ForgeVersionList process(byte[] arg) throws IOException {
					return ForgeVersionList.fromJson(new JSONObject(new String(arg, "UTF-8")));
				}
			});
		} catch (URISyntaxException e) {
			throw new IllegalStateException("unable to convert to URI", e);
		}
	}

	@Override
	public DownloadTask<Object> gameVersionJson(final MinecraftDirectory mcdir, final String version) {
		if (!FORGE_VERSION_PATTERN.matcher(version).matches()) {
			return null;
		}
		// 5 - length of "forge"
		String forgeversion = version.substring(version.indexOf("forge") + 5);
		try {
			return new MemoryDownloadTask(new URI("http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + forgeversion + "/forge-" + forgeversion + "-installer.jar")).andThen(new ResultProcessor<byte[], JSONObject>() {

				@Override
				public JSONObject process(byte[] arg) throws IOException {
					JSONObject json = null;
					try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(arg))) {
						ZipEntry entry;
						while ((entry = in.getNextEntry()) != null) {
							if ("install_profile.json".equals(entry.getName())) {
								json = new JSONObject(new JSONTokener(new InputStreamReader(in, "UTF-8")));
								in.closeEntry();
								break;
							}
							in.closeEntry();
						}
					}
					return json;
				}
			}).andThen(new ResultProcessor<JSONObject, Object>() {

				@Override
				public Object process(JSONObject arg) throws IOException {
					JSONObject versionjson = arg.getJSONObject("versionInfo");
					File jsonfile = mcdir.getVersionJson(version);
					if (!jsonfile.getParentFile().exists()) {
						jsonfile.getParentFile().mkdirs();
					}
					try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(jsonfile)), "UTF-8")) {
						writer.write(versionjson.toString(4));
					}
					return null;
				}
			});
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected URI getLibrary(Library library) {
		if ("net.minecraftforge".equals(library.getDomain()) && "forge".equals(library.getName())) {
			String version = library.getVersion();
			try {
				return new URI("http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + version + "/forge-" + version + "-universal.jar");
			} catch (URISyntaxException e) {
				e.printStackTrace();
				// ignore
			}
		}
		return null;
	}

	@Override
	protected URI getGameJar(String version) {
		return null;
	}

	@Override
	protected URI getGameVersionJson(String version) {
		return null;
	}

	@Override
	protected URI getAssetIndex(String version) {
		return null;
	}

	@Override
	protected URI getVersionList() {
		return null;
	}

	@Override
	protected URI getAsset(Asset asset) {
		return null;
	}

}
