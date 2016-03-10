package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
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
import org.to2mbn.jmccc.auth.yggdrasil.core.util.Base64;

public class SignaturedPropertiesDeserializer implements Serializable, PropertiesDeserializer {

	private static PublicKey loadDefaultSignaturePublicKey() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		ByteArrayOutputStream byteout = new ByteArrayOutputStream();
		try (InputStream in = SignaturedPropertiesDeserializer.class.getResourceAsStream("/yggdrasil_session_pubkey.der")) {
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

	private static final long serialVersionUID = 1L;

	private PublicKey signaturePublicKey;

	public SignaturedPropertiesDeserializer() {
		try {
			signaturePublicKey = loadDefaultSignaturePublicKey();
		} catch (InvalidKeySpecException | NoSuchAlgorithmException | IOException e) {
			throw new IllegalStateException("failed to load default yggdrasil signature key", e);
		}
	}

	public SignaturedPropertiesDeserializer(PublicKey signaturePublicKey) {
		this.signaturePublicKey = signaturePublicKey;
	}

	@Override
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
				}
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

	private void checkSignature(String key, String value, String signature) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
		Signature verifier = Signature.getInstance("SHA1withRSA");
		verifier.initVerify(signaturePublicKey);
		verifier.update(value.getBytes());
		if (!verifier.verify(Base64.decode(signature.toCharArray()))) {
			throw new SignatureException("invalid signature");
		}
	}

}
