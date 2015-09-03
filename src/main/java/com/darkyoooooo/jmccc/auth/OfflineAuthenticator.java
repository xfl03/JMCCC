package com.darkyoooooo.jmccc.auth;

import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.UUID;
import com.darkyoooooo.jmccc.launch.AuthenticationException;
import com.darkyoooooo.jmccc.util.Utils;

public class OfflineAuthenticator implements Authenticator {

    private String playerName;

    /**
     * Creates an OfflineAuthenticator.
     * 
     * @param playerName the offline player name
     * @throws NullPointerException if <code>playerName==null</code>
     * @throws IllegalArgumentException if <code>playerName.length()==0</code>
     */
    public OfflineAuthenticator(String playerName) {
        Objects.requireNonNull(playerName);
        this.playerName = playerName;

        if (this.playerName.length() == 0) {
            throw new IllegalArgumentException("Zero length player name");
        }
    }

    @Override
    public AuthResult auth() throws AuthenticationException {
        try {
            return new AuthResult(playerName, Utils.generateRandomToken(), generateUUID().toString().replace("-", ""), "{}", "mojang");
        } catch (UnsupportedEncodingException e) {
            throw new AuthenticationException("UTF-8 is not supported", e);
        }
    }

    private UUID generateUUID() throws UnsupportedEncodingException {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes("UTF-8"));
    }
}
