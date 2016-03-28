package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.to2mbn.jmccc.auth.yggdrasil.core.util.Base64;

public class PropertiesDeserializer implements Serializable {

	private static final long serialVersionUID = 1L;

	private PublicKey signaturePublicKey;

	public PropertiesDeserializer(PublicKey signaturePublicKey) {
		this.signaturePublicKey = signaturePublicKey;
	}

	public Map<String, String> toProperties(JSONArray props, boolean forceSignature) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException, JSONException {
		if (props == null) {
			return null;
		}

		Map<String, String> properties = new HashMap<>();
		for (int i = 0; i < props.length(); i++) {
			JSONObject prop = props.getJSONObject(i);
			String key = prop.getString("name");
			String value = prop.getString("value");
			if (prop.has("signature")) {
				if (signaturePublicKey == null) {
					if (forceSignature) {
						throw new InvalidKeyException("no key is available");
					} else {
						continue;
					}
				} else {
					String signature = prop.getString("signature");
					checkSignature(key, value, signature);
				}
			} else {
				if (forceSignature) {
					throw new SignatureException("no signature");
				}
			}
			properties.put(key, value);
		}
		return properties;
	}

	private void checkSignature(String key, String value, String signature) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
		Signature verifier = Signature.getInstance("SHA1withRSA");
		verifier.initVerify(signaturePublicKey);
		verifier.update(value.getBytes());
		if (!verifier.verify(Base64.decode(signature.toCharArray()))) {
			throw new SignatureException("invalid signature");
		}
	}

}
