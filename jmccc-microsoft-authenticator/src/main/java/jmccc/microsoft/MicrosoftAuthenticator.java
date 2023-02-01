package jmccc.microsoft;

import jmccc.microsoft.core.MicrosoftAuthenticationController;
import jmccc.microsoft.entity.MicrosoftSession;
import jmccc.microsoft.entity.MicrosoftVerification;
import jmccc.microsoft.entity.MinecraftProfile;
import org.to2mbn.jmccc.auth.AuthInfo;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.Authenticator;
import org.to2mbn.jmccc.util.UUIDUtils;

import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;

public class MicrosoftAuthenticator implements Authenticator {
    private static String clientId;
    private MinecraftProfile profile;

    private MicrosoftSession session;

    private final MicrosoftAuthenticationController controller;

    private MicrosoftAuthenticator() {
        this.controller = new MicrosoftAuthenticationController(clientId);
    }


    /**
     * Create with exist token
     * This method will take a long time and block current thread, please run in new thread
     *
     * @param session  token used in authenticator
     * @param callback a callback to display verification link and code to user
     * @return MicrosoftAuthenticator
     */
    public static MicrosoftAuthenticator session(MicrosoftSession session, Consumer<MicrosoftVerification> callback) throws AuthenticationException {
        MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
        MicrosoftAuthenticationController controller = authenticator.controller;
        authenticator.session = session;

        //Get profile by token
        try {
            authenticator.profile = controller.getMinecraftProfile(session);
            return authenticator;
        } catch (AuthenticationException ignored) {
        }

        //Refresh tokens
        try {
            session = controller.refreshMicrosoftToken(session);
            session = controller.getMinecraftToken(session);
            authenticator.session = session;
            authenticator.profile = controller.getMinecraftProfile(session);
            return authenticator;
        } catch (AuthenticationException ignored) {
        }

        //Token is expired, login again
        return login(callback);
    }

    /**
     * Create with new login
     * This method will take a long time and block current thread, please run in new thread
     *
     * @param callback a callback to display verification link and code to user
     * @return MicrosoftAuthenticator
     */
    public static MicrosoftAuthenticator login(Consumer<MicrosoftVerification> callback) throws AuthenticationException {
        Objects.requireNonNull(callback);
        MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
        MicrosoftAuthenticationController controller = authenticator.controller;

        //Get token and profile
        authenticator.session = controller.getMinecraftToken(controller.getMicrosoftToken(callback));
        authenticator.profile = controller.getMinecraftProfile(authenticator.session);
        return authenticator;
    }

    /**
     * Get auth info
     *
     * @return auth info
     */
    @Override
    public AuthInfo auth() {
        Objects.requireNonNull(profile);

        return new AuthInfo(profile.name, session.minecraftAccessToken, UUIDUtils.toUUID(profile.id),
                Collections.emptyMap(), "msa", session.xboxUserId);
    }

    /**
     * Get token used in authenticator, which can be saved
     *
     * @return token used in authenticator
     */
    public MicrosoftSession getSession() {
        return session;
    }

    /**
     * Set custom Microsoft Azure client id
     * <a href="https://learn.microsoft.com/en-us/azure/active-directory/develop/quickstart-register-app">How to get client id</a>
     *
     * @param clientId custom client id
     */
    public static void setClientId(String clientId) {
        MicrosoftAuthenticator.clientId = clientId;
    }
}
