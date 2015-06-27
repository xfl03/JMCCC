package com.darkyoooooo.jmccc.auth;

public interface IAuthenticator {
	public String getType();
	public AuthInfo run();
}
