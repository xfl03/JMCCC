package org.to2mbn.jmccc.auth.yggdrasil.core.texture;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface Texture {

    Map<String, String> getMetadata();

    InputStream openStream() throws IOException;

}
