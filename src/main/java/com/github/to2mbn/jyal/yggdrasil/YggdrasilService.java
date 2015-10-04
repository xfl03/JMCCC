package com.github.to2mbn.jyal.yggdrasil;

import org.json.JSONException;
import org.json.JSONObject;
import com.github.to2mbn.jyal.AuthenticationException;
import com.github.to2mbn.jyal.RemoteAuthenticationException;
import com.github.to2mbn.jyal.io.JSONHttpRequester;

abstract public class YggdrasilService {

	protected final JSONHttpRequester requester = new JSONHttpRequester();

	protected void checkResponse(JSONObject response) throws AuthenticationException {
		if (response == null) {
			throw new AuthenticationException("empty response");
		}

		try {
			if (response.has("error") && !response.getString("error").isEmpty()) {
				throw new RemoteAuthenticationException(response.getString("error"), response.optString("errorMessage"), response.optString("cause"));
			}
		} catch (JSONException e) {
			throw newResponseFormatException(e);
		}
	}

	protected AuthenticationException newResponseFormatException(Throwable e) {
		return new AuthenticationException("wrong response format", e);
	}

	protected AuthenticationException newRequestFailedException(Throwable e) {
		return new AuthenticationException("failed to request", e);
	}

	protected AuthenticationException newSignatureException(Throwable e) {
		return new AuthenticationException("failed to verify signature", e);
	}

}
