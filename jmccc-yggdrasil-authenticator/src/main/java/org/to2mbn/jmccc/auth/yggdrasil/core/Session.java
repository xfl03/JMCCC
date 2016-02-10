package org.to2mbn.jmccc.auth.yggdrasil.core;

import java.util.Map;

public interface Session {

	String getUserID();

	UserType getUserType();

	Map<String, String> getUserProperties();

	String getAccessToken();

	GameProfile[] getGameProfiles();

	GameProfile getSelectedGameProfile();

}
