package com.darkyoooooo.jmccc.auth;

import com.darkyoooooo.jmccc.launch.AuthenticationException;

public interface Authenticator {

    /**
     * Do the authentication and return the result of authentication.
     * 
     * @return the authentication
     * @throws AuthenticationException if an exception has occurred during authentication
     */
    AuthResult auth() throws AuthenticationException;

}
