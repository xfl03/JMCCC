package com.github.to2mbn.jyal;

import java.util.Map;

public interface SessionService {

	Session login(String username, String password) throws AuthenticationException;

	Session loginWithToken(String token) throws AuthenticationException;

	boolean isValid(String token) throws AuthenticationException;

	Map<String, String> getProfileProperties(GameProfile profile) throws AuthenticationException;

	PlayerTextures getTextures(Map<String, String> profileProperties) throws AuthenticationException;

}
