package org.to2mbn.jyal;

import java.util.Map;

public interface Session {

	String getUserID();

	UserType getUserType();

	Map<String, String> getUserProperties();

	String getAccessToken();

	GameProfile[] getGameProfiles();

	GameProfile getSelectedGameProfile();

}
