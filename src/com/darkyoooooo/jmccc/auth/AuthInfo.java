package com.darkyoooooo.jmccc.auth;

import lombok.Getter;

public class AuthInfo {
	@Getter private String uuid, displayName, accessToken, properties, error, userType;
	
	public AuthInfo(String uuid, String displayName, String accessToken,
			String properties, String error, String userType) {
		this.uuid = uuid;
		this.displayName = displayName;
		this.accessToken = accessToken;
		this.properties = properties;
		this.error = error;
		this.userType = userType;
	}
}
