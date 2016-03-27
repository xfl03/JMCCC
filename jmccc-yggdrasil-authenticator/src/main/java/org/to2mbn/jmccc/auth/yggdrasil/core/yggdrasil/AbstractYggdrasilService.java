package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import org.json.JSONException;
import org.json.JSONObject;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.core.RemoteAuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.core.io.JSONHttpRequester;

abstract public class AbstractYggdrasilService {

	private JSONHttpRequester requester;
	private PropertiesDeserializer propertiesDeserializer;
	private YggdrasilAPIProvider api;

	public AbstractYggdrasilService(JSONHttpRequester requester, PropertiesDeserializer propertiesDeserializer, YggdrasilAPIProvider api) {
		this.requester = requester;
		this.propertiesDeserializer = propertiesDeserializer;
		this.api = api;
	}

	protected void requireNonEmptyResponse(JSONObject response) throws AuthenticationException {
		if (response == null) {
			throw new AuthenticationException("empty response");
		}
		tryThrowRemoteException(response);
	}

	protected void requireEmptyResponse(JSONObject response) throws AuthenticationException {
		if (response == null) {
			return;
		}

		tryThrowRemoteException(response);
		throw new ResponseFormatException("invalid response: " + response);
	}

	protected JSONHttpRequester getRequester() {
		return requester;
	}

	protected PropertiesDeserializer getPropertiesDeserializer() {
		return propertiesDeserializer;
	}

	protected YggdrasilAPIProvider getApi() {
		return api;
	}

	private void tryThrowRemoteException(JSONObject response) throws AuthenticationException {
		try {
			if (response.has("error") && !response.getString("error").isEmpty()) {
				throw new RemoteAuthenticationException(response.getString("error"), response.optString("errorMessage", null), response.optString("cause", null));
			}
		} catch (JSONException e) {
			throw new ResponseFormatException(e);
		}
	}

}
