package org.to2mbn.jmccc.mcdownloader.provider.processors;

import org.json.JSONObject;
import org.to2mbn.jmccc.mcdownloader.download.tasks.ResultProcessor;
import org.to2mbn.jmccc.util.IOUtils;

public class JsonProcessor implements ResultProcessor<byte[], JSONObject> {

	@Override
	public JSONObject process(byte[] data) throws Exception {
		return IOUtils.toJson(data);
	}

}
