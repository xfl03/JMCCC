package org.to2mbn.jmccc.auth;

public interface Authenticator {

	/**
	 * Authenticate and return the result information of the authentication.
	 * 
	 * @return the result information of the authentication
	 * @throws AuthenticationException when an exception occurred during the authentication
	 */
	AuthInfo auth() throws AuthenticationException;

}
