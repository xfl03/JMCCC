package org.to2mbn.jmccc.mcdownloader.provider.forge;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.to2mbn.jmccc.internal.org.json.JSONObject;
import org.to2mbn.jmccc.internal.org.json.JSONTokener;
import org.to2mbn.jmccc.mcdownloader.download.tasks.ResultProcessor;
import org.to2mbn.jmccc.mcdownloader.provider.VersionJsonInstaller;
import org.to2mbn.jmccc.option.MinecraftDirectory;

class InstallProfileProcessor implements ResultProcessor<byte[], String> {

	private MinecraftDirectory mcdir;

	public InstallProfileProcessor(MinecraftDirectory mcdir) {
		this.mcdir = mcdir;
	}

	@Override
	public String process(byte[] arg) throws Exception {
		String version = null;
		try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(arg))) {
			ZipEntry entry;
			while ((entry = in.getNextEntry()) != null) {
				if ("install_profile.json".equals(entry.getName())) {
					version = new VersionJsonInstaller(mcdir).process((processJson(new JSONObject(new JSONTokener(new InputStreamReader(in, "UTF-8"))))));
					in.closeEntry();
					break;
				}
				in.closeEntry();
			}
		}

		if (version == null) {
			throw new IllegalArgumentException("No install_profile.json has found");
		}

		return version;
	}

	protected JSONObject processJson(JSONObject installprofile) {
		return installprofile.getJSONObject("versionInfo");
	}

}
