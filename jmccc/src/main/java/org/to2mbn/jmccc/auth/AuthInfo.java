package org.to2mbn.jmccc.auth;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class AuthInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String username;
	private String token;
	private String uuid;
	private Map<String, String> properties;
	private String userType;

	/**
	 * Constructs an AuthInfo.
	 * 
	 * @param username the username
	 * @param token the access token
	 * @param uuid the uuid of the login
	 * @param properties the properties
	 * @param userType the type of the login
	 * @throws NullPointerException if any of the params is null
	 */
	public AuthInfo(String username, String token, String uuid, Map<String, String> properties, String userType) {
		Objects.requireNonNull(username);
		Objects.requireNonNull(token);
		Objects.requireNonNull(uuid);
		Objects.requireNonNull(properties);
		Objects.requireNonNull(userType);

		this.username = username;
		this.token = token;
		this.uuid = uuid;
		this.properties = properties;
		this.userType = userType;
	}

	/**
	 * Gets the username.
	 * 
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Gets the access token.
	 * 
	 * @return the access token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Gets the uuid of the login.
	 * 
	 * @return the uuid of the login
	 */
	public String getUUID() {
		return uuid;
	}

	/**
	 * Gets the properties.
	 * 
	 * @return the properties
	 */
	public Map<String, String> getProperties() {
		return properties;
	}

	/**
	 * Gets the type of the login.
	 * 
	 * @return the type of the login
	 */
	public String getUserType() {
		return userType;
	}

	@Override
	public String toString() {
		return String.format("AuthInfo [username=%s, token=%s, uuid=%s, properties=%s, userType=%s]", username, token, uuid, properties, userType);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof AuthInfo) {
			AuthInfo another = (AuthInfo) obj;
			return username.equals(another.username) && token.equals(another.token) && uuid.equals(another.uuid) && properties.equals(another.properties) && userType.equals(another.userType);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(username, token, uuid, properties, userType);
	}

}
