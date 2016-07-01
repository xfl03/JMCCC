package org.to2mbn.jmccc.mcdownloader.provider;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.to2mbn.jmccc.internal.org.json.JSONObject;
import org.to2mbn.jmccc.mcdownloader.download.tasks.ResultProcessor;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.util.FileUtils;

public class VersionJsonInstaller implements ResultProcessor<JSONObject, String> {

	private MinecraftDirectory mcdir;

	public VersionJsonInstaller(MinecraftDirectory mcdir) {
		this.mcdir = mcdir;
	}

	@Override
	public String process(JSONObject json) throws Exception {
		String version = json.getString("id");
		File target = mcdir.getVersionJson(version);
		FileUtils.prepareWrite(target);
		try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(target)), "UTF-8")) {
			writer.write(json.toString(4));
		}
		return version;
	}

}
