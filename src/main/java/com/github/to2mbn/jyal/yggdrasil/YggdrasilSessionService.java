package com.github.to2mbn.jyal.yggdrasil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
import com.github.to2mbn.jyal.Agent;
import com.github.to2mbn.jyal.AuthenticationException;
import com.github.to2mbn.jyal.GameProfile;
import com.github.to2mbn.jyal.PlayerTextures;
import com.github.to2mbn.jyal.Session;
import com.github.to2mbn.jyal.SessionService;
import com.github.to2mbn.jyal.Texture;
import com.github.to2mbn.jyal.io.JSONHttpRequester;
import com.github.to2mbn.jyal.util.Base64;
import com.github.to2mbn.jyal.util.UUIDUtils;

public class YggdrasilSessionService implements SessionService {

	private static final String API_AUTHENTICATE = "https://authserver.mojang.com/authenticate";
	private static final String API_REFRESH = "https://authserver.mojang.com/refresh";
	private static final String API_VALIDATE = "https://authserver.mojang.com/validate";
	private static final String API_PROFILE = "https://sessionserver.mojang.com/session/minecraft/profile/";

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
		return handleAuthResponse(response);
	}

	@Override
	public Session loginWithToken(String token) throws AuthenticationException {
		Map<String, Object> request = new HashMap<>();
		request.put("clientToken", clientToken);
		request.put("accessToken", token);
		request.put("requestUser", true);
		JSONObject response;
		try {
			response = requester.jsonPost(API_REFRESH, null, new JSONObject(request));
		} catch (JSONException | IOException e) {
			throw new AuthenticationException("failed to request", e);
		}
		return handleAuthResponse(response);
	}

	private void checkResponse(JSONObject response) throws AuthenticationException {
		if (response == null) {
			throw new AuthenticationException("empty response");
		}
		if (response.has("error") && !response.getString("error").isEmpty()) {
			StringBuilder sb = new StringBuilder(response.getString("error"));
			if (response.has("errorMessage")) {
				sb.append(": ");
				sb.append(response.getString("errorMessage"));
			}
			if (response.has("cause")) {
				sb.append(": ");
				sb.append(response.getString("cause"));
			}
			throw new AuthenticationException(sb.toString());
		}
	}

	private GameProfile getGameProfile(JSONObject gameprofileResponse) {
		if (gameprofileResponse == null) {
			return null;
		}
		return new GameProfile(UUIDUtils.fromUUIDString(gameprofileResponse.getString("id")), gameprofileResponse.getString("name"));
	}

	private void checkSignature(String key, String value, String signature) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
		Signature verifier = Signature.getInstance("SHA1withRSA");
		verifier.initVerify(signaturePublicKey);
		verifier.update(value.getBytes());
		if (!verifier.verify(Base64.decode(signature.toCharArray()))) {
			throw new SignatureException("signature verify failed");
		}
	}

	private Map<String, String> getProperties(JSONArray props, boolean forceSignature) throws AuthenticationException {
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

	private Session handleAuthResponse(JSONObject response) throws JSONException, AuthenticationException {
		checkResponse(response);
		if (!clientToken.equals(response.getString("clientToken"))) {
			throw new AuthenticationException("clientToken changed from " + clientToken + " to " + response.getString("clientToken"));
		}

		String accessToken = response.getString("accessToken");

		JSONObject userjson = response.getJSONObject("user");
		String userId = userjson.getString("id");
		Map<String, String> userProperties = getProperties(userjson.optJSONArray("properties"), false);

		GameProfile selectedProfile = getGameProfile(response.optJSONObject("selectedProfile"));

		JSONArray profilesArray = response.optJSONArray("availableProfiles");
		GameProfile[] availableProfiles;
		if (profilesArray == null) {
			availableProfiles = null;
		} else {
			availableProfiles = new GameProfile[profilesArray.length()];
			for (int i = 0; i < profilesArray.length(); i++) {
				availableProfiles[i] = getGameProfile(profilesArray.getJSONObject(i));
			}
		}

		return new YggdrasilSession(userId, userProperties, accessToken, availableProfiles, selectedProfile);
	}

	@Override
	public boolean isValid(String token) throws AuthenticationException {
		Map<String, Object> request = new HashMap<>();
		request.put("clientToken", clientToken);
		request.put("accessToken", token);
		JSONObject response;
		try {
			response = requester.jsonPost(API_VALIDATE, null, new JSONObject(request));
		} catch (JSONException | IOException e) {
			throw new AuthenticationException("failed to request", e);
		}
		if (response == null) {
			return true;
		} else if ("ForbiddenOperationException".equals(response.optString("error"))) {
			return false;
		} else {
			// try to handle this response as a remote exception
			checkResponse(response);

			// invalid response
			// it isn't null and doesn't include any error message
			throw new AuthenticationException("invalid response: " + response);
		}
	}

	@Override
	public Map<String, String> getProfileProperties(GameProfile profile) throws AuthenticationException {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put("unsigned", "false");
		JSONObject response;
		try {
			response = requester.jsonGet(API_PROFILE + UUIDUtils.toUnsignedUUIDString(profile.getUUID()), arguments);
		} catch (JSONException | IOException e) {
			throw new AuthenticationException("failed to request", e);
		}
		checkResponse(response);
		return getProperties(response.optJSONArray("properties"), true);
	}

	@Override
	public PlayerTextures getTextures(Map<String, String> profileProperties) throws AuthenticationException {
		String encodedTextures = profileProperties.get("textures");
		if (encodedTextures == null) {
			return null;
		}
		JSONObject response;
		try {
			response = new JSONObject(new String(Base64.decode(encodedTextures.toCharArray()), "UTF-8"));
		} catch (JSONException | UnsupportedEncodingException e) {
			throw new AuthenticationException("failed to resolve response", e);
		}
		JSONObject textures = response.getJSONObject("textures");
		return new PlayerTextures(getTexture(textures.optJSONObject("SKIN")), getTexture(textures.optJSONObject("CAPE")));
	}

	private Texture getTexture(JSONObject json) {
		if (json == null) {
			return null;
		}

		String url = json.getString("url");
		Map<String, String> metadata = null;
		if (json.has("metadata")) {
			metadata = new HashMap<>();
			JSONObject metadataJson = json.getJSONObject("metadata");
			for (Object rawtypeKey : metadataJson.keySet()) {
				String key = (String) rawtypeKey;
				String value = metadataJson.getString(key);
				metadata.put(key, value);
			}
		}
		return new Texture(url, metadata);
	}
}
