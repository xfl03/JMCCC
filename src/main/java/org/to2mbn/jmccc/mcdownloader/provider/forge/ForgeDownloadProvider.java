package org.to2mbn.jmccc.mcdownloader.provider.forge;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import org.json.JSONObject;
import org.to2mbn.jmccc.mcdownloader.download.DownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.MemoryDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.ResultProcessor;
import org.to2mbn.jmccc.mcdownloader.download.multiple.MultipleDownloadTask;
import org.to2mbn.jmccc.mcdownloader.provider.InstallProfileProcessor;
import org.to2mbn.jmccc.mcdownloader.provider.URIDownloadProvider;
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
	public MultipleDownloadTask<Object> gameVersionJson(final MinecraftDirectory mcdir, final String version) {
		if (!FORGE_VERSION_PATTERN.matcher(version).matches()) {
			return null;
		}
		// 5 - length of "forge"
		String forgeversion = version.substring(version.indexOf("forge") + 5);
		try {
			return MultipleDownloadTask.simple(new MemoryDownloadTask(new URI("http://files.minecraftforge.net/maven/net/minecraftforge/forge/" + forgeversion + "/forge-" + forgeversion + "-installer.jar")).andThen(new InstallProfileProcessor(mcdir.getVersionJson(version))));
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
