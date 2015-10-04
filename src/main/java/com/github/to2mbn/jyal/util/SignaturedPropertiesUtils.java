package com.github.to2mbn.jyal.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class SignaturedPropertiesUtils {

	private static PublicKey signaturePublicKey;

	static {
		try {
			signaturePublicKey = loadSignaturePublicKey();
		} catch (GeneralSecurityException | IOException e) {
			throw new SecurityException("couldn't read signature public key", e);
		}
	}

	public static Map<String, String> toProperties(JSONArray props, boolean forceSignature) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException, JSONException {
		if (props == null) {
			return null;
		}

		Map<String, String> properties = new HashMap<>();
		for (int i = 0; i < props.length(); i++) {
			JSONObject prop = props.getJSONObject(i);
			String key = prop.getString("name");
			String value = prop.getString("value");
			if (prop.has("signature")) {
				String signature = prop.getString("signature");
				checkSignature(key, value, signature);
			} else {
				if (forceSignature) {
					throw new SignatureException("no signature");
				}
			}
			properties.put(key, value);
		}
		return properties;
	}

	private static void checkSignature(String key, String value, String signature) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
		Signature verifier = Signature.getInstance("SHA1withRSA");
		verifier.initVerify(signaturePublicKey);
		verifier.update(value.getBytes());
		if (!verifier.verify(Base64.decode(signature.toCharArray()))) {
			throw new SignatureException("signature verify failed");
		}
	}

	private static PublicKey loadSignaturePublicKey() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		ByteArrayOutputStream byteout = new ByteArrayOutputStream();
		try (InputStream in = SignaturedPropertiesUtils.class.getResourceAsStream("/yggdrasil_session_pubkey.der")) {
			byte[] buffer = new byte[8192];
			int read;
			while ((read = in.read(buffer)) != -1) {
				byteout.write(buffer, 0, read);
			}
		}

		X509EncodedKeySpec spec = new X509EncodedKeySpec(byteout.toByteArray());
		KeyFactory keyFactory;

		keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePublic(spec);
	}

	private SignaturedPropertiesUtils() {
	}

}
