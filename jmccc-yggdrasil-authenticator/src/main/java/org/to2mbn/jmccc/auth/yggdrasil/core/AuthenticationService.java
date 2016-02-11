package org.to2mbn.jmccc.auth.yggdrasil.core;

import java.util.UUID;
import org.to2mbn.jmccc.auth.AuthenticationException;

public interface AuthenticationService {

	Session login(String username, String password) throws AuthenticationException;

	Session refresh(String accessToken) throws AuthenticationException;

	Session selectProfile(String accessToken, UUID profile) throws AuthenticationException;

	boolean validate(String accessToken) throws AuthenticationException;

}
