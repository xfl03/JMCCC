package org.to2mbn.jmccc.auth.yggdrasil.core;

import org.to2mbn.jmccc.auth.AuthenticationException;

public interface SessionService {

	Session login(String username, String password) throws AuthenticationException;

	Session loginWithToken(String token) throws AuthenticationException;

	boolean isValid(String token) throws AuthenticationException;

}
