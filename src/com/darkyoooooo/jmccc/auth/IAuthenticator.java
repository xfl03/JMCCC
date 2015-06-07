package com.darkyoooooo.jmccc.auth;

public interface IAuthenticator {
	String getType();
	AuthInfo run();
}
