package com.github.to2mbn.jmccc.auth;

import java.io.IOException;
import java.util.Objects;
import com.github.to2mbn.jmccc.launch.AuthenticationException;
import net.kronos.mclaunch_util_lib.auth.YggdrasilRequester;
import net.kronos.mclaunch_util_lib.auth.model.YggdrasilAgent;
import net.kronos.mclaunch_util_lib.auth.model.YggdrasilAuthenticateRes;
import net.kronos.mclaunch_util_lib.auth.model.YggdrasilError;
import net.kronos.mclaunch_util_lib.auth.model.YggdrasilProfile;

public class YggdrasilAuthenticator implements Authenticator {

    private String email;
    private String password;

    /**
     * Creates a YggdrasilAuthenticator.
     * 
     * @param email the email
     * @param password the password
     * @throws NullPointerException if <code>email==null||password==null</code>
     */
    public YggdrasilAuthenticator(String email, String password) {
        Objects.requireNonNull(email);
        Objects.requireNonNull(password);
        this.email = email;
        this.password = password;
    }

    @Override
    public AuthResult auth() throws AuthenticationException {
        YggdrasilRequester req = new YggdrasilRequester();

        YggdrasilAuthenticateRes res;
        try {
            res = req.authenticate(YggdrasilAgent.getMinecraftAgent(), email, password);
        } catch (IOException | YggdrasilError e) {
            throw new AuthenticationException("Failed to auth", e);
        }

        YggdrasilProfile profile = res.getSelectedProfile();
        return new AuthResult(profile.getName(), res.getAccessToken(), profile.getId(), "{}", profile.isLegacy() ? "legacy" : "mojang");
    }

}
