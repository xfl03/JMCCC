package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import org.to2mbn.jmccc.auth.yggdrasil.core.util.Base64;
import org.to2mbn.jmccc.internal.org.json.JSONArray;
import org.to2mbn.jmccc.internal.org.json.JSONException;
import org.to2mbn.jmccc.internal.org.json.JSONObject;

import java.io.Serializable;
import java.security.*;
import java.util.Map;
import java.util.TreeMap;

class PropertiesDeserializer implements Serializable {

    private static final long serialVersionUID = 1L;

    private PublicKey signaturePublicKey;

    public PropertiesDeserializer(PublicKey signaturePublicKey) {
        this.signaturePublicKey = signaturePublicKey;
    }

    public Map<String, String> toProperties(JSONArray props, boolean forceSignature) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException, JSONException {
        Map<String, String> properties = new TreeMap<>();
        for (int i = 0; i < props.length(); i++) {
            JSONObject prop = props.getJSONObject(i);
            String key = prop.getString("name");
            String value = prop.getString("value");
            if (prop.has("signature")) {
                if (signaturePublicKey == null) {
                    if (forceSignature) {
                        throw new InvalidKeyException("No available key");
                    } else {
                        continue;
                    }
                } else {
                    String signature = prop.getString("signature");
                    checkSignature(value, signature);
                }
            } else {
                if (forceSignature) {
                    throw new SignatureException("No available signature");
                }
            }
            properties.put(key, value);
        }
        return properties;
    }

    private void checkSignature(String value, String signature) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        Signature verifier = Signature.getInstance("SHA1withRSA");
        verifier.initVerify(signaturePublicKey);
        verifier.update(value.getBytes());
        if (!verifier.verify(Base64.decode(signature.toCharArray()))) {
            throw new SignatureException("Invalid signature. data=[" + value + "], expectedSignature=[" + signature + "]");
        }
    }

}
