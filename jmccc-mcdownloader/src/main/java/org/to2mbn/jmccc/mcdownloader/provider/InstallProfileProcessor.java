package org.to2mbn.jmccc.mcdownloader.provider;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.to2mbn.jmccc.mcdownloader.download.ResultProcessor;
import org.to2mbn.jmccc.option.MinecraftDirectory;

public class InstallProfileProcessor implements ResultProcessor<byte[], String> {

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
					version = writeJson(processJson(new JSONObject(new JSONTokener(new InputStreamReader(in, "UTF-8")))));
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

	private String writeJson(JSONObject json) throws IOException {
		String version = json.getString("id");
		File output = mcdir.getVersionJson(version);

		if (!output.getParentFile().exists()) {
			output.getParentFile().mkdirs();
		}
		try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(output)), "UTF-8")) {
			writer.write(json.toString(4));
		}

		return version;
	}

}
