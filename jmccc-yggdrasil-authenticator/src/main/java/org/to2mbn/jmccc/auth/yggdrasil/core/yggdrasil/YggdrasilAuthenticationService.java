package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.core.Agent;
import org.to2mbn.jmccc.auth.yggdrasil.core.AuthenticationService;
import org.to2mbn.jmccc.auth.yggdrasil.core.GameProfile;
import org.to2mbn.jmccc.auth.yggdrasil.core.RemoteAuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.core.Session;
import org.to2mbn.jmccc.auth.yggdrasil.core.UserType;
import org.to2mbn.jmccc.auth.yggdrasil.core.io.JSONHttpRequester;
import org.to2mbn.jmccc.util.UUIDUtils;

public class YggdrasilAuthenticationService extends AbstractYggdrasilService implements AuthenticationService {

	private Agent agent;

	public YggdrasilAuthenticationService(JSONHttpRequester requester, PropertiesDeserializer propertiesDeserializer, YggdrasilAPIProvider api, Agent agent) {
		super(requester, propertiesDeserializer, api);
		this.agent = agent;
	}

	@Override
	public Session login(String username, String password, String clientToken) throws AuthenticationException {
		Objects.requireNonNull(username);
		Objects.requireNonNull(password);
		Objects.requireNonNull(clientToken);

		Map<String, Object> request = new HashMap<>();
		request.put("agent", agent);
		request.put("username", username);
		request.put("password", password);
		request.put("clientToken", clientToken);
		request.put("requestUser", true);
		JSONObject response;
		try {
			response = (JSONObject) getRequester().jsonPost(getApi().authenticate(), null, new JSONObject(request));
		} catch (JSONException | IOException e) {
			throw new RequestException(e);
		}
		return handleAuthResponse(response, clientToken);
	}

	@Override
	public Session refresh(String clientToken, String accessToken) throws AuthenticationException {
		return selectProfile(clientToken, accessToken, null);
	}

	@Override
	public Session selectProfile(String clientToken, String accessToken, UUID profile) throws AuthenticationException {
		Objects.requireNonNull(clientToken);
		Objects.requireNonNull(accessToken);

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
			response = (JSONObject) getRequester().jsonPost(getApi().refresh(), null, new JSONObject(request));
		} catch (JSONException | IOException e) {
			throw new RequestException(e);
		}
		return handleAuthResponse(response, clientToken);
	}

	@Override
	public boolean validate(String accessToken) throws AuthenticationException {
		return validate(null, accessToken);
	}

	@Override
	public boolean validate(String clientToken, String accessToken) throws AuthenticationException {
		Objects.requireNonNull(accessToken);

		Map<String, Object> request = new HashMap<>();
		if (clientToken != null) {
			request.put("clientToken", clientToken);
		}
		request.put("accessToken", accessToken);
		JSONObject response;
		try {
			response = (JSONObject) getRequester().jsonPost(getApi().validate(), null, new JSONObject(request));
		} catch (JSONException | IOException e) {
			throw new RequestException(e);
		}

		try {
			requireEmptyResponse(response);
		} catch (RemoteAuthenticationException e) {
			if ("ForbiddenOperationException".equals(e.getRemoteExceptionName())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void invalidate(String clientToken, String accessToken) throws AuthenticationException {
		Objects.requireNonNull(clientToken);
		Objects.requireNonNull(accessToken);

		Map<String, Object> request = new HashMap<>();
		request.put("clientToken", clientToken);
		request.put("accessToken", accessToken);
		JSONObject response;
		try {
			response = (JSONObject) getRequester().jsonPost(getApi().invalidate(), null, new JSONObject(request));
		} catch (JSONException | IOException e) {
			throw new RequestException(e);
		}
		requireEmptyResponse(response);
	}

	@Override
	public void signout(String username, String password) throws AuthenticationException {
		Objects.requireNonNull(username);
		Objects.requireNonNull(password);

		Map<String, Object> request = new HashMap<>();
		request.put("username", username);
		request.put("password", password);
		JSONObject response;
		try {
			response = (JSONObject) getRequester().jsonPost(getApi().signout(), null, new JSONObject(request));
		} catch (JSONException | IOException e) {
			throw new RequestException(e);
		}
		requireEmptyResponse(response);
	}

	public Agent getAgent() {
		return agent;
	}

	private Session handleAuthResponse(JSONObject response, String clientToken) throws AuthenticationException {
		requireNonEmptyResponse(response);

		try {
			if (clientToken != null && !clientToken.equals(response.getString("clientToken"))) {
				throw new AuthenticationException("clientToken changed from " + clientToken + " to " + response.getString("clientToken"));
			}

			String accessToken = response.getString("accessToken");

			JSONObject userjson = response.getJSONObject("user");
			String userId = userjson.getString("id");
			Map<String, String> userProperties;
			try {
				userProperties = getPropertiesDeserializer().toProperties(userjson.optJSONArray("properties"), false);
			} catch (GeneralSecurityException e) {
				throw new ResponseSignatureException(e);
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
			return new Session(clientToken, accessToken, selectedProfile, availableProfiles, userId, userProperties, UserType.MOJANG);
		} catch (JSONException e) {
			throw new ResponseFormatException(e);
		}
	}

	private GameProfile toGameProfile(JSONObject gameprofileResponse) throws JSONException {
		if (gameprofileResponse == null) {
			return null;
		}

		return new GameProfile(UUIDUtils.toUUID(gameprofileResponse.getString("id")), gameprofileResponse.getString("name"));
	}

}
