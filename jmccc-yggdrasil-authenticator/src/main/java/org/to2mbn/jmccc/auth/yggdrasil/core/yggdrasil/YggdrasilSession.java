package org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil;

import java.util.Arrays;
import java.util.Map;
import org.to2mbn.jmccc.auth.yggdrasil.core.GameProfile;
import org.to2mbn.jmccc.auth.yggdrasil.core.Session;
import org.to2mbn.jmccc.auth.yggdrasil.core.UserType;

public class YggdrasilSession implements Session {

	private String userId;
	private Map<String, String> properties;
	private String accessToken;
	private GameProfile[] profiles;
	private GameProfile selectedProfile;

	public YggdrasilSession(String userId, Map<String, String> properties, String accessToken, GameProfile[] profiles, GameProfile selectedProfile) {
		this.userId = userId;
		this.properties = properties;
		this.accessToken = accessToken;
		this.profiles = profiles;
		this.selectedProfile = selectedProfile;
	}

	@Override
	public String getUserID() {
		return userId;
	}

	@Override
	public UserType getUserType() {
		return UserType.MOJANG;
	}

	@Override
	public Map<String, String> getUserProperties() {
		return properties;
	}

	@Override
	public String getAccessToken() {
		return accessToken;
	}

	@Override
	public GameProfile[] getGameProfiles() {
		return profiles;
	}

	@Override
	public GameProfile getSelectedGameProfile() {
		return selectedProfile;
	}

	@Override
	public String toString() {
		return "YggdrasilSession [userId=" + userId + ", properties=" + properties + ", accessToken=" + accessToken + ", profiles=" + Arrays.toString(profiles) + ", selectedProfile=" + selectedProfile + "]";
	}

}
