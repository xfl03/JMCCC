package org.to2mbn.jyal.yggdrasil;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.to2mbn.jyal.Agent;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jyal.GameProfile;
import org.to2mbn.jyal.Session;
import org.to2mbn.jyal.SessionService;
import org.to2mbn.jyal.util.SignaturedPropertiesUtils;
import org.to2mbn.jyal.util.UUIDUtils;

public class YggdrasilSessionService extends YggdrasilService implements SessionService {

	private static final String API_AUTHENTICATE = "https://authserver.mojang.com/authenticate";
	private static final String API_REFRESH = "https://authserver.mojang.com/refresh";
	private static final String API_VALIDATE = "https://authserver.mojang.com/validate";

	private String clientToken;
	private Agent agent;

	public YggdrasilSessionService(UUID clientToken, Agent agent) {
		this.clientToken = UUIDUtils.unsign(clientToken);
		this.agent = agent;
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
			throw newRequestFailedException(e);
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
			throw newRequestFailedException(e);
		}
		return handleAuthResponse(response);
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
			throw newRequestFailedException(e);
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

	private Session handleAuthResponse(JSONObject response) throws AuthenticationException {
		checkResponse(response);

		try {
			if (!clientToken.equals(response.getString("clientToken"))) {
				throw new AuthenticationException("clientToken changed from " + clientToken + " to " + response.getString("clientToken"));
			}

			String accessToken = response.getString("accessToken");

			JSONObject userjson = response.getJSONObject("user");
			String userId = userjson.getString("id");
			Map<String, String> userProperties;
			try {
				userProperties = SignaturedPropertiesUtils.toProperties(userjson.optJSONArray("properties"), false);
			} catch (GeneralSecurityException e) {
				throw newSignatureException(e);
			}

			GameProfile selectedProfile = toGameProfile(response.optJSONObject("selectedProfile"));

			JSONArray profilesArray = response.optJSONArray("availableProfiles");
			GameProfile[] availableProfiles;
			if (profilesArray == null) {
				availableProfiles = null;
			} else {
				availableProfiles = new GameProfile[profilesArray.length()];
				for (int i = 0; i < profilesArray.length(); i++) {
					availableProfiles[i] = toGameProfile(profilesArray.getJSONObject(i));
				}
			}
			return new YggdrasilSession(userId, userProperties, accessToken, availableProfiles, selectedProfile);
		} catch (JSONException e) {
			throw newResponseFormatException(e);
		}
	}

	private GameProfile toGameProfile(JSONObject gameprofileResponse) throws JSONException {
		if (gameprofileResponse == null) {
			return null;
		}

		return new GameProfile(UUIDUtils.fromString(gameprofileResponse.getString("id")), gameprofileResponse.getString("name"));
	}

}
