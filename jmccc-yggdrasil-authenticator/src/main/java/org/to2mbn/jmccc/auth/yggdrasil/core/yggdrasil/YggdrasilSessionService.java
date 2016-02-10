package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.core.Agent;
import org.to2mbn.jmccc.auth.yggdrasil.core.GameProfile;
import org.to2mbn.jmccc.auth.yggdrasil.core.Session;
import org.to2mbn.jmccc.auth.yggdrasil.core.SessionService;
import org.to2mbn.jmccc.auth.yggdrasil.core.UserType;
import org.to2mbn.jmccc.auth.yggdrasil.core.util.SignaturedPropertiesUtils;
import org.to2mbn.jmccc.auth.yggdrasil.core.util.UUIDUtils;

public class YggdrasilSessionService extends YggdrasilService implements SessionService {

	private static final String API_AUTHENTICATE = "https://authserver.mojang.com/authenticate";
	private static final String API_REFRESH = "https://authserver.mojang.com/refresh";
	private static final String API_VALIDATE = "https://authserver.mojang.com/validate";

	private String clientToken;
	private Agent agent;

	public YggdrasilSessionService(String clientToken, Agent agent) {
		this.clientToken = clientToken;
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
	public Session refresh(String accessToken) throws AuthenticationException {
		return selectProfile(accessToken, null);
	}

	@Override
	public Session selectProfile(String accessToken, UUID profile) throws AuthenticationException {
		Map<String, Object> request = new HashMap<>();
		request.put("clientToken", clientToken);
		request.put("accessToken", accessToken);
		request.put("requestUser", true);

		if (profile != null) {
			JSONObject selectedProfile = new JSONObject();
			selectedProfile.put("id", UUIDUtils.unsign(profile));
			request.put("selectedProfile", selectedProfile);
		}

		JSONObject response;
		try {
			response = requester.jsonPost(API_REFRESH, null, new JSONObject(request));
		} catch (JSONException | IOException e) {
			throw newRequestFailedException(e);
		}
		return handleAuthResponse(response);
	}

	@Override
	public boolean validate(String accessToken) throws AuthenticationException {
		Map<String, Object> request = new HashMap<>();
		request.put("clientToken", clientToken);
		request.put("accessToken", accessToken);
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
			return new Session(userId, accessToken, selectedProfile, availableProfiles, userId, userProperties, UserType.MOJANG);
		} catch (JSONException e) {
			throw newResponseFormatException(e);
		}
	}

	private GameProfile toGameProfile(JSONObject gameprofileResponse) throws JSONException {
		if (gameprofileResponse == null) {
			return null;
		}

		return new GameProfile(UUIDUtils.toUUID(gameprofileResponse.getString("id")), gameprofileResponse.getString("name"));
	}

}
