package org.to2mbn.jmccc.auth;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Describes the result of authentication.
 *
 * @author yushijinhun
 */
public class AuthInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String username;
    private final String token;
    private final UUID uuid;
    private final Map<String, String> properties;
    private final String userType;
    private final String xboxUserId;

    /**
     * Constructs an AuthInfo.
     *
     * @param username   the username
     * @param token      the access token
     * @param uuid       the uuid of the login
     * @param properties the properties
     * @param userType   the type of the login
     * @throws NullPointerException if any of the params is null
     */
    public AuthInfo(String username, String token, UUID uuid, Map<String, String> properties, String userType) {
        this(username, token, uuid, properties, userType, "");
    }

    /**
     * Constructs an AuthInfo with Xbox User ID.
     *
     * @param username   the username
     * @param token      the access token
     * @param uuid       the uuid of the login
     * @param properties the properties
     * @param userType   the type of the login
     * @param xboxUserId Xbox User ID(XUID)
     * @throws NullPointerException if any of the params is null
     */
    public AuthInfo(String username, String token, UUID uuid, Map<String, String> properties, String userType, String xboxUserId) {
        Objects.requireNonNull(username);
        Objects.requireNonNull(token);
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(properties);
        Objects.requireNonNull(userType);
        Objects.requireNonNull(xboxUserId);

        this.username = username;
        this.token = token;
        this.uuid = uuid;
        this.properties = properties;
        this.userType = userType;
        this.xboxUserId = xboxUserId;
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
    public UUID getUUID() {
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

    /**
     * Get Xbox User ID
     *
     * @return Xbox User ID
     */
    public String getXboxUserId() {
        return xboxUserId;
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
            return Objects.equals(username, another.username)
                    && Objects.equals(token, another.token)
                    && Objects.equals(uuid, another.uuid)
                    && Objects.equals(properties, another.properties)
                    && Objects.equals(userType, another.userType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }

}
