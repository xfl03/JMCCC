package org.to2mbn.jmccc.auth;

public interface Authenticator {

    /**
     * Authenticates and returns the result of authentication.
     *
     * @return the result of authentication
     * @throws AuthenticationException if an exception occurs during
     *                                 authentication
     */
    AuthInfo auth() throws AuthenticationException;

}
