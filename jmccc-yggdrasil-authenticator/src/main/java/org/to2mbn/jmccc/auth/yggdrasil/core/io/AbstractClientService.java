package org.to2mbn.jmccc.auth.yggdrasil.core.io;

import java.util.concurrent.Callable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.core.RemoteAuthenticationException;

public class AbstractClientService {

	protected final JSONHttpRequester requester;

	public AbstractClientService(JSONHttpRequester requester) {
		this.requester = requester;
	}

	protected JSONObject requireJsonObject(Object response) throws AuthenticationException, JSONException {
		if (response instanceof JSONObject) {
			JSONObject cast = (JSONObject) response;
			tryThrowRemoteException(cast);
			return cast;
		}
		throw new JSONException("Illegal response: " + response);
	}

	protected JSONArray requireJsonArray(Object response) throws AuthenticationException, JSONException {
		if (response instanceof JSONArray) {
			return (JSONArray) response;
		} else if (response instanceof JSONObject) {
			tryThrowRemoteException((JSONObject) response);
		}
		throw new JSONException("Illegal response: " + response);
	}

	protected void requireEmpty(Object response) throws AuthenticationException, JSONException {
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
