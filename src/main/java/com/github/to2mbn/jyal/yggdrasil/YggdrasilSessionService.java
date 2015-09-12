package com.github.to2mbn.jyal.yggdrasil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.github.to2mbn.jyal.Agent;
import com.github.to2mbn.jyal.AuthenticationException;
import com.github.to2mbn.jyal.GameProfile;
import com.github.to2mbn.jyal.Session;
import com.github.to2mbn.jyal.SessionService;
import com.github.to2mbn.jyal.io.JSONHttpRequester;

public class YggdrasilSessionService implements SessionService {

	private static final String API_AUTHENTICATE = "https://authserver.mojang.com/authenticate";
	private static final String API_PROFILE = "https://authserver.mojang.com/profile/";

	private String clientToken;
	private Agent agent;
	private JSONHttpRequester requester;
	private PublicKey signaturePublicKey;

	public YggdrasilSessionService(String clientToken, Agent agent) {
		this(clientToken, agent, new JSONHttpRequester());
	}

	public YggdrasilSessionService(String clientToken, Agent agent, JSONHttpRequester requester) {
		this.clientToken = clientToken;
		this.agent = agent;
		this.requester = requester;
		loadSignaturePublicKey();
	}

	private void loadSignaturePublicKey() {
		try {
			ByteArrayOutputStream byteout = new ByteArrayOutputStream();
			try (InputStream in = getClass().getResourceAsStream("/yggdrasil_session_pubkey.der")) {
				byte[] buffer = new byte[8192];
				int read;
				while ((read = in.read(buffer)) != -1) {
					byteout.write(buffer, 0, read);
				}
			}

			X509EncodedKeySpec spec = new X509EncodedKeySpec(byteout.toByteArray());
			KeyFactory keyFactory;

			keyFactory = KeyFactory.getInstance("RSA");
			signaturePublicKey = keyFactory.generatePublic(spec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			throw new SecurityException("Missing/invalid yggdrasil public key!", e);
		}

	}

	@Override
	public Session login(String username, String password) throws AuthenticationException {
		Map<String, Object> request = new HashMap<>();
		request.put("agent", agent);
		request.put("username", username);
		request.put("password", password);
		request.put("clientToken", clientToken);
		request.put("requestUser", true);
		JSONObject response;
		try {
			response = requester.jsonPost(API_AUTHENTICATE, null, new JSONObject(request));
		} catch (JSONException | IOException e) {
			throw new AuthenticationException("failed to request", e);
		}
		checkResponse(response);
		if (!clientToken.equals(response.getString("clientToken"))) {
			throw new AuthenticationException("clientToken changed from " + clientToken + " to " + response.getString("clientToken"));
		}
		JSONObject userjson = response.getJSONObject("user");

		String accessToken = response.getString("accessToken");
		String userId = userjson.getString("id");
		Map<String, String> userProperties = userjson.has("properties") ? getProperties(userjson.getJSONArray("properties"), false) : null;
		GameProfile selectedProfile = getGameProfile(response.getJSONObject("selectedProfile"));

		JSONArray profilesArray = response.getJSONArray("availableProfiles");
		GameProfile[] availableProfiles = new GameProfile[profilesArray.length()];
		for (int i = 0; i < profilesArray.length(); i++) {
			availableProfiles[i] = getGameProfile(profilesArray.getJSONObject(i));
		}

		return new YggdrasilSession(userId, userProperties, accessToken, availableProfiles, selectedProfile);
	}

	private void checkResponse(JSONObject response) throws AuthenticationException {
		if (response.has("error") && !response.getString("error").isEmpty()) {
			throw new AuthenticationException("remote server error: " + response.getString("error") + ":" + (response.has("errorMessage") ? response.getString("errorMessage") : null) + ":" + (response.has("cause") ? response.getString("cause") : null));
		}
	}

	private GameProfile getGameProfile(JSONObject gameprofileResponse) throws AuthenticationException {
		return getGameProfile(UUID.fromString(gameprofileResponse.getString("id")));
	}

	private GameProfile getGameProfile(UUID uuid) throws AuthenticationException {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put("unsigned", false);
		JSONObject response;
		try {
			response = requester.jsonGet(API_PROFILE + uuid.toString().replace("-", ""), arguments);
		} catch (JSONException | IOException e) {
			throw new AuthenticationException("failed to request", e);
		}
		checkResponse(response);
		return new GameProfile(UUID.fromString(response.getString("id")), response.getString("name"), false, getProperties(response.getJSONArray("properties"), true));
	}

	private void checkSignature(String key, String value, String signature) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
		Signature verifier = Signature.getInstance("SHA1withRSA");
		verifier.initVerify(signaturePublicKey);
		verifier.update(value.getBytes());
		if (!verifier.verify(Base64.getDecoder().decode(signature))) {
			throw new SignatureException("signature verify failed");
		}
	}

	private Map<String, String> getProperties(JSONArray props, boolean forceSignature) throws AuthenticationException {
		Map<String, String> properties = new HashMap<>();
		for (int i = 0; i < props.length(); i++) {
			JSONObject prop = props.getJSONObject(i);
			String key = prop.getString("name");
			String value = prop.getString("value");
			if (prop.has("signature")) {
				String signature = prop.getString("signature");
				try {
					checkSignature(key, value, signature);
				} catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException e) {
					throw new AuthenticationException("signature verify failed", e);
				}
			} else {
				if (forceSignature) {
					throw new AuthenticationException("no signature");
				}
			}
			properties.put(key, value);
		}
		return properties;
	}

	public String getClientToken() {
		return clientToken;
	}

	public Agent getAgent() {
		return agent;
	}

}
