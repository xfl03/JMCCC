package org.to2mbn.jmccc.auth.yggdrasil.core;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class Session implements Serializable {

	private static final long serialVersionUID = 1L;

	private String clientToken;
	private String accessToken;
	private GameProfile selectedProfile;
	private GameProfile[] profiles;
	private String userId;
	private Map<String, String> properties;
	private UserType userType;

	public Session(String clientToken, String accessToken, GameProfile selectedProfile, GameProfile[] profiles, String userId, Map<String, String> properties, UserType userType) {
		this.clientToken = clientToken;
		this.accessToken = accessToken;
		this.selectedProfile = selectedProfile;
		this.profiles = profiles;
		this.userId = userId;
		this.properties = properties;
		this.userType = userType;
	}

	public String getClientToken() {
		return clientToken;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public GameProfile getSelectedProfile() {
		return selectedProfile;
	}

	public GameProfile[] getProfiles() {
		return profiles;
	}

	public String getUserId() {
		return userId;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public UserType getUserType() {
		return userType;
	}

	@Override
	public int hashCode() {
		return Arrays.deepHashCode(new Object[] { clientToken, accessToken, selectedProfile, profiles, userId, properties, userType });
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Session) {
			Session another = (Session) obj;
			return Objects.equals(getClientToken(), another.getClientToken()) &&
					Objects.equals(getAccessToken(), another.getAccessToken()) &&
					Objects.equals(getSelectedProfile(), another.getSelectedProfile()) &&
					Objects.deepEquals(getProfiles(), another.getProfiles()) &&
					Objects.equals(getProfiles(), another.getProperties()) &&
					Objects.equals(getUserId(), another.getUserId()) &&
					Objects.equals(getUserType(), another.getUserType());
		}
		return false;
	}

}
