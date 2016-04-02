package org.to2mbn.jmccc.auth;

public interface Authenticator {

	/**
	 * Authenticates and returns the result of the authentication.
	 * 
	 * @return the result of the authentication
	 * @throws AuthenticationException when an exception occurred during the
	 *             authentication
	 */
	AuthInfo auth() throws AuthenticationException;

}
