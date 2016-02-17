package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import java.security.GeneralSecurityException;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;

public interface PropertiesDeserializer {

	Map<String, String> toProperties(JSONArray props, boolean forceSignature) throws GeneralSecurityException, JSONException;

}
