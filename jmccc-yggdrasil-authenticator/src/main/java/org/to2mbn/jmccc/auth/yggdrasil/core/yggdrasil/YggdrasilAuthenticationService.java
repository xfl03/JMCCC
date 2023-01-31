package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.core.*;
import org.to2mbn.jmccc.auth.yggdrasil.core.io.HttpRequester;
import org.to2mbn.jmccc.internal.org.json.JSONArray;
import org.to2mbn.jmccc.internal.org.json.JSONObject;
import org.to2mbn.jmccc.util.UUIDUtils;

import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import static org.to2mbn.jmccc.auth.yggdrasil.core.io.HttpUtils.CONTENT_TYPE_JSON;

class YggdrasilAuthenticationService extends AbstractYggdrasilService implements AuthenticationService {

    private Agent agent;

    public YggdrasilAuthenticationService(HttpRequester requester, PropertiesDeserializer propertiesDeserializer, YggdrasilAPIProvider api, Agent agent) {
        super(requester, propertiesDeserializer, api);
        this.agent = agent;
    }

    @Override
    public Session login(String username, String password, final String clientToken) throws AuthenticationException {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        Objects.requireNonNull(clientToken);

        final Map<String, Object> request = new HashMap<>();
        request.put("agent", agent);
        request.put("username", username);
        request.put("password", password);
        request.put("clientToken", clientToken);
        request.put("requestUser", true);

        return invokeOperation(new Callable<Session>() {

            @Override
            public Session call() throws Exception {
                return handleAuthResponse(
                        requester.requestWithPayload("POST", api.authenticate(), new JSONObject(request), CONTENT_TYPE_JSON),
                        clientToken);
            }
        });
    }

    @Override
    public Session refresh(String clientToken, String accessToken) throws AuthenticationException {
        return selectProfile(clientToken, accessToken, null);
    }

    @Override
    public Session selectProfile(final String clientToken, String accessToken, GameProfile profile) throws AuthenticationException {
        Objects.requireNonNull(clientToken);
        Objects.requireNonNull(accessToken);

        final Map<String, Object> request = new HashMap<>();
        request.put("clientToken", clientToken);
        request.put("accessToken", accessToken);
        request.put("requestUser", true);

        if (profile != null) {
            JSONObject selectedProfile = new JSONObject();
            selectedProfile.put("id", UUIDUtils.unsign(profile.getUUID()));
            selectedProfile.put("name", profile.getName());
            request.put("selectedProfile", selectedProfile);
        }

        return invokeOperation(new Callable<Session>() {

            @Override
            public Session call() throws Exception {
                return handleAuthResponse(
                        requester.requestWithPayload("POST", api.refresh(), new JSONObject(request), CONTENT_TYPE_JSON),
                        clientToken);
            }
        });
    }

    @Override
    public boolean validate(String accessToken) throws AuthenticationException {
        return validate(null, accessToken);
    }

    @Override
    public boolean validate(String clientToken, String accessToken) throws AuthenticationException {
        Objects.requireNonNull(accessToken);

        final Map<String, Object> request = new HashMap<>();
        if (clientToken != null) {
            request.put("clientToken", clientToken);
        }
        request.put("accessToken", accessToken);

        return invokeOperation(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                try {
                    requireEmpty(requester.requestWithPayload("POST", api.validate(), new JSONObject(request), CONTENT_TYPE_JSON));
                    return true;
                } catch (RemoteAuthenticationException e) {
                    if ("ForbiddenOperationException".equals(e.getRemoteExceptionName())) {
                        return false;
                    }
                    throw e;
                }
            }
        });
    }

    @Override
    public void invalidate(String clientToken, String accessToken) throws AuthenticationException {
        Objects.requireNonNull(clientToken);
        Objects.requireNonNull(accessToken);

        final Map<String, Object> request = new HashMap<>();
        request.put("clientToken", clientToken);
        request.put("accessToken", accessToken);

        invokeOperation(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                requireEmpty(requester.requestWithPayload("POST", api.invalidate(), new JSONObject(request), CONTENT_TYPE_JSON));
                return null;
            }
        });
    }

    @Override
    public void signout(String username, String password) throws AuthenticationException {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);

        final Map<String, Object> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);

        invokeOperation(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                requireEmpty(requester.requestWithPayload("POST", api.signout(), new JSONObject(request), CONTENT_TYPE_JSON));
                return null;
            }
        });
    }

    public Agent getAgent() {
        return agent;
    }

    private Session handleAuthResponse(Object rawResponse, String clientToken) throws AuthenticationException, GeneralSecurityException {
        JSONObject response = requireJsonObject(rawResponse);

        if (clientToken != null && !clientToken.equals(response.getString("clientToken"))) {
            throw new AuthenticationException("clientToken changed from " + clientToken + " to " + response.getString("clientToken"));
        }

        String accessToken = response.getString("accessToken");

        JSONObject userjson = response.optJSONObject("user");
        String userId = userjson == null ? null : userjson.optString("id", null);

        Map<String, String> userProperties;
        JSONArray propertiesJson = userjson == null ? null : userjson.optJSONArray("properties");
        if (propertiesJson == null) {
            userProperties = null;
        } else {
            userProperties = Collections.unmodifiableMap(propertiesDeserializer.toProperties(propertiesJson, false));
        }

        GameProfile selectedProfile = parseGameProfile(response.optJSONObject("selectedProfile"));

        JSONArray profilesArray = response.optJSONArray("availableProfiles");
        GameProfile[] availableProfiles;
        if (profilesArray == null) {
            availableProfiles = null;
        } else {
            availableProfiles = new GameProfile[profilesArray.length()];
            for (int i = 0; i < profilesArray.length(); i++) {
                availableProfiles[i] = parseGameProfile(profilesArray.getJSONObject(i));
            }
        }
        return new Session(clientToken, accessToken, selectedProfile, availableProfiles, userId, userProperties, UserType.MOJANG);
    }

}
