package com.darkyoooooo.jmccc.auth;

public class AuthInfo {
    private String uuid, displayName, accessToken, properties, error, userType;

    public AuthInfo(String uuid, String displayName, String accessToken, String properties, String userType) {
        this.uuid = uuid;
        this.displayName = displayName;
        this.accessToken = accessToken;
        this.properties = properties;
        this.userType = userType;
    }

    public AuthInfo(String error) {
        this.error = error;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getProperties() {
        return this.properties;
    }

    public String getError() {
        return this.error;
    }

    public String getUserType() {
        return this.userType;
    }
}
