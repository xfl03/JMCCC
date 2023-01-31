package org.to2mbn.jmccc.mcdownloader.provider;

import org.to2mbn.jmccc.internal.org.json.JSONObject;
import org.to2mbn.jmccc.mcdownloader.download.tasks.ResultProcessor;
import org.to2mbn.jmccc.util.IOUtils;

public class JsonDecoder implements ResultProcessor<byte[], JSONObject> {

    @Override
    public JSONObject process(byte[] data) throws Exception {
        return IOUtils.toJson(data);
    }

}
