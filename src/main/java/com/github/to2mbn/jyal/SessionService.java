package com.github.to2mbn.jyal;

public interface SessionService {

	Session login(String username, String password) throws AuthenticationException;

}
