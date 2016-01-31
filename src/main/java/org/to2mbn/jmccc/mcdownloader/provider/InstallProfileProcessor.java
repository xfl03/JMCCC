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

public class InstallProfileProcessor implements ResultProcessor<byte[], Object> {

	private File output;

	public InstallProfileProcessor(File output) {
		this.output = output;
	}

	@Override
	public Object process(byte[] arg) throws Exception {
		try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(arg))) {
			ZipEntry entry;
			while ((entry = in.getNextEntry()) != null) {
				if ("install_profile.json".equals(entry.getName())) {
					writeJson(processJson(new JSONObject(new JSONTokener(new InputStreamReader(in, "UTF-8")))));
					in.closeEntry();
					break;
				}
				in.closeEntry();
			}
		}
		return null;
	}

	protected JSONObject processJson(JSONObject installprofile) {
		return installprofile.getJSONObject("versionInfo");
	}

	private void writeJson(JSONObject json) throws IOException {
		if (!output.getParentFile().exists()) {
			output.getParentFile().mkdirs();
		}
		try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(output)), "UTF-8")) {
			writer.write(json.toString(4));
		}
	}

}
