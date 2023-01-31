package org.to2mbn.jmccc.auth.yggdrasil.test;

import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.core.AuthenticationService;
import org.to2mbn.jmccc.auth.yggdrasil.core.GameProfile;
import org.to2mbn.jmccc.auth.yggdrasil.core.Session;
import org.to2mbn.jmccc.auth.yggdrasil.core.UserType;
import org.to2mbn.jmccc.util.UUIDUtils;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class MockAuthenticationService implements AuthenticationService {

    public String e_username = "user";
    public String e_password = "password";
    public String e_clientToken;
    public String e_accessToken;
    public GameProfile e_selectedProfile;
    public GameProfile[] e_profiles = new GameProfile[]{
            createGameProfile("player1"),
            createGameProfile("player2")
    };
    public String e_userId = "userid";
    public Map<String, String> e_properties;
    public UserType e_userType = UserType.MOJANG;
    public boolean tokenAvailable = true;

    public static GameProfile createGameProfile(String name) {
        try {
            return new GameProfile(UUID.nameUUIDFromBytes(name.getBytes("UTF-8")), name);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Session login(String username, String password, String clientToken) throws AuthenticationException {
        if (e_username.equals(username) && e_password.equals(password)) {
            e_clientToken = clientToken;
            e_accessToken = UUIDUtils.randomUnsignedUUID();
            tokenAvailable = true;
            return session();
        } else {
            throw new AuthenticationException("wrong username/password");
        }
    }

    @Override
    public Session refresh(String clientToken, String accessToken) throws AuthenticationException {
        return selectProfile(clientToken, accessToken, null);
    }

    @Override
    public Session selectProfile(String clientToken, String accessToken, GameProfile profile) throws AuthenticationException {
        if (Objects.equals(e_accessToken, accessToken) && Objects.equals(e_clientToken, clientToken)) {
            e_clientToken = clientToken;
            e_accessToken = UUIDUtils.randomUnsignedUUID();
            tokenAvailable = true;
            if (profile != null) {
                boolean selected = false;
                for (GameProfile p : e_profiles) {
                    if (p.getUUID().equals(profile.getUUID())) {
                        e_selectedProfile = p;
                        selected = true;
                    }
                }
                if (!selected) {
                    throw new AuthenticationException("no such profile: " + profile);
                }
            }
            return session();
        } else {
            throw new AuthenticationException("wrong token");
        }
    }

    @Override
    public boolean validate(String accessToken) throws AuthenticationException {
        return tokenAvailable && Objects.equals(e_accessToken, accessToken);
    }

    @Override
    public boolean validate(String clientToken, String accessToken) throws AuthenticationException {
        return tokenAvailable && Objects.equals(e_accessToken, accessToken) && Objects.equals(e_clientToken, clientToken);
    }

    @Override
    public void invalidate(String clientToken, String accessToken) throws AuthenticationException {
        if (Objects.equals(e_accessToken, accessToken) && Objects.equals(e_clientToken, clientToken)) {
            e_clientToken = null;
            e_accessToken = null;
        } else {
            throw new AuthenticationException("wrong token");
        }
    }

    @Override
    public void signout(String username, String password) throws AuthenticationException {
        if (e_username.equals(username) && e_password.equals(password)) {
            e_clientToken = null;
            e_accessToken = null;
        } else {
            throw new AuthenticationException("wrong username/password");
        }
    }

    public Session session() {
        return new Session(e_clientToken, e_accessToken, e_selectedProfile, e_profiles, e_userId, e_properties, e_userType);
    }

}
