package org.to2mbn.jmccc.auth.yggdrasil.core.io;

import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.core.RemoteAuthenticationException;
import org.to2mbn.jmccc.internal.org.json.JSONArray;
import org.to2mbn.jmccc.internal.org.json.JSONException;
import org.to2mbn.jmccc.internal.org.json.JSONObject;
import org.to2mbn.jmccc.internal.org.json.JSONTokener;

import java.util.concurrent.Callable;

public class AbstractClientService {

    protected final HttpRequester requester;

    public AbstractClientService(HttpRequester requester) {
        this.requester = requester;
    }

    private static Object toJsonIfNecessary(Object obj) throws JSONException {
        if (obj instanceof String) {
            return toJson((String) obj);
        } else {
            return obj;
        }
    }

    private static Object toJson(String json) throws JSONException {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            JSONTokener tokener = new JSONTokener(json);
            char next = tokener.nextClean();
            tokener.back();
            if (next == '{') {
                return new JSONObject(tokener);
            } else if (next == '[') {
                return new JSONArray(tokener);
            }
        } catch (JSONException e) {
            throw new JSONException("Couldn't resolve json: " + json, e);
        }

        throw new JSONException("Couldn't resolve json: " + json);
    }

    protected JSONObject nullableJsonObject(Object response) throws AuthenticationException, JSONException {
        response = toJsonIfNecessary(response);
        if (response == null) {
            return null;
        } else {
            return requireJsonObject(response);
        }
    }

    protected JSONArray nullableJsonArray(Object response) throws AuthenticationException, JSONException {
        response = toJsonIfNecessary(response);
        if (response == null) {
            return null;
        } else {
            return requireJsonArray(response);
        }
    }

    protected JSONObject requireJsonObject(Object response) throws AuthenticationException, JSONException {
        response = toJsonIfNecessary(response);

        if (response instanceof JSONObject) {
            JSONObject cast = (JSONObject) response;
            tryThrowRemoteException(cast);
            return cast;
        }
        throw new JSONException("Illegal response: " + response);
    }

    protected JSONArray requireJsonArray(Object response) throws AuthenticationException, JSONException {
        response = toJsonIfNecessary(response);

        if (response instanceof JSONArray) {
            return (JSONArray) response;
        } else if (response instanceof JSONObject) {
            tryThrowRemoteException((JSONObject) response);
        }
        throw new JSONException("Illegal response: " + response);
    }

    protected void requireEmpty(Object response) throws AuthenticationException, JSONException {
        response = toJsonIfNecessary(response);

        if (response == null) {
            return;
        }

        if (response instanceof JSONObject) {
            tryThrowRemoteException((JSONObject) response);
        }
        throw new JSONException("Illegal response: " + response);
    }

    protected <R> R invokeOperation(Callable<R> operation) throws AuthenticationException {
        try {
            return operation.call();
        } catch (Exception e) {
            if (e instanceof AuthenticationException) {
                throw (AuthenticationException) e;
            } else {
                throw new AuthenticationException(e);
            }
        }
    }

    private void tryThrowRemoteException(JSONObject response) throws AuthenticationException, JSONException {
        try {
            if (response.has("error") && !response.getString("error").isEmpty()) {
                throw new RemoteAuthenticationException(response.getString("error"), response.optString("errorMessage", null), response.optString("cause", null));
            }
        } catch (JSONException e) {
            throw new JSONException("Couldn't parse response: " + response, e);
        }
    }

}
