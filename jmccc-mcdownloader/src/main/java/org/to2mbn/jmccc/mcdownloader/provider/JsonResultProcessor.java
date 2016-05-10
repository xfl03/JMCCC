package org.to2mbn.jmccc.mcdownloader.provider;

import org.json.JSONObject;
import org.to2mbn.jmccc.mcdownloader.download.ResultProcessor;
import org.to2mbn.jmccc.util.IOUtils;

public class JsonResultProcessor implements ResultProcessor<byte[], JSONObject> {

	@Override
	public JSONObject process(byte[] data) throws Exception {
		return IOUtils.toJson(data);
	}

}
