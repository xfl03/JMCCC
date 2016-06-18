package org.to2mbn.jmccc.auth;

public interface Authenticator {

	/**
	 * Authenticates and returns the result information of the authentication.
	 * 
	 * @return the result information of the authentication
	 * @throws AuthenticationException if an exception occurs during
	 *             authentication
	 */
	AuthInfo auth() throws AuthenticationException;

}
