package com.github.to2mbn.jmccc.auth;

import com.github.to2mbn.jmccc.launch.AuthenticationException;

public interface Authenticator {

	/**
	 * Do the authentication and return the result of authentication.
	 * 
	 * @return the authentication
	 * @throws AuthenticationException if an exception has occurred during authentication
	 */
	AuthInfo auth() throws AuthenticationException;

}
