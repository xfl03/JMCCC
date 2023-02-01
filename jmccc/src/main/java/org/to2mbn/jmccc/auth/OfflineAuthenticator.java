package org.to2mbn.jmccc.auth;

import org.to2mbn.jmccc.util.UUIDUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class OfflineAuthenticator implements Authenticator, Serializable {

    private static final long serialVersionUID = 1L;

    private final String playerName;

    /**
     * Constructs an OfflineAuthenticator.
     *
     * @param playerName the player name
     * @throws NullPointerException     if <code>playerName==null</code>
     * @throws IllegalArgumentException if <code>playerName.length()==0</code>
     */
    public OfflineAuthenticator(String playerName) {
        Objects.requireNonNull(playerName);
        this.playerName = playerName;

        if (this.playerName.length() == 0) {
            throw new IllegalArgumentException("Player name cannot be empty");
        }
    }

    @Override
    public AuthInfo auth() throws AuthenticationException {
        try {
            return new AuthInfo(playerName, UUIDUtils.randomUnsignedUUID(), getPlayerUUID(), Collections.unmodifiableMap(new HashMap<>()), "mojang");
        } catch (UnsupportedEncodingException e) {
            throw new AuthenticationException("UTF-8 is not supported", e);
        }
    }

    private UUID getPlayerUUID() throws UnsupportedEncodingException {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String toString() {
        return "OfflineAuthenticator[" + playerName + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof OfflineAuthenticator) {
            OfflineAuthenticator another = (OfflineAuthenticator) obj;
            return playerName.equals(another.playerName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return playerName.hashCode();
    }

    public static OfflineAuthenticator name(String playerName) {
        return new OfflineAuthenticator(playerName);
    }
}
