package org.to2mbn.jmccc.auth.yggdrasil.core;

import org.to2mbn.jmccc.auth.AuthenticationException;

public interface AuthenticationService {

    Session login(String username, String password, String clientToken) throws AuthenticationException;

    Session refresh(String clientToken, String accessToken) throws AuthenticationException;

    Session selectProfile(String clientToken, String accessToken, GameProfile profile) throws AuthenticationException;

    boolean validate(String accessToken) throws AuthenticationException;

    boolean validate(String clientToken, String accessToken) throws AuthenticationException;

    void invalidate(String clientToken, String accessToken) throws AuthenticationException;

    void signout(String username, String password) throws AuthenticationException;

}
