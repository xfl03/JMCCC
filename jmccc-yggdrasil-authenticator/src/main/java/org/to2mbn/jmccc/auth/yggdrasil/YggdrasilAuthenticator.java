package org.to2mbn.jmccc.auth.yggdrasil;

import org.to2mbn.jmccc.auth.AuthInfo;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.Authenticator;
import org.to2mbn.jmccc.auth.yggdrasil.core.AuthenticationService;
import org.to2mbn.jmccc.auth.yggdrasil.core.GameProfile;
import org.to2mbn.jmccc.auth.yggdrasil.core.Session;
import org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil.YggdrasilAuthenticationServiceBuilder;
import org.to2mbn.jmccc.util.UUIDUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Provides yggdrasil authentication feature.
 *
 * @author yushijinhun
 */
public class YggdrasilAuthenticator implements Authenticator, SessionCredential, Serializable {

    private static final long serialVersionUID = 1L;
    private transient AuthenticationService authenticationService;
    private volatile Session authResult;

    /**
     * Constructs a YggdrasilAuthenticator with a random client token.
     */
    public YggdrasilAuthenticator() {
        this(YggdrasilAuthenticationServiceBuilder.buildDefault());
    }

    /**
     * Constructs a YggdrasilAuthenticator with a customized
     * {@link AuthenticationService}.
     *
     * @param sessionService the customized {@link AuthenticationService}
     */
    public YggdrasilAuthenticator(AuthenticationService sessionService) {
        Objects.requireNonNull(sessionService);
        this.authenticationService = sessionService;
    }

    public static PasswordProvider createPasswordProvider(final String username, final String password, final CharacterSelector characterSelector) {
        return new PasswordProvider() {

            @Override
            public String getUsername() throws AuthenticationException {
                return username;
            }

            @Override
            public String getPassword() throws AuthenticationException {
                return password;
            }

            @Override
            public CharacterSelector getCharacterSelector() {
                return characterSelector;
            }
        };
    }

    /**
     * Creates a <code>YggdrasilAuthenticator</code> and initializes it with a
     * token.
     *
     * @param clientToken the client token
     * @param accessToken the access token
     * @return a YggdrasilAuthenticator
     * @throws AuthenticationException If an exception occurs during the
     *                                 authentication
     */
    public static YggdrasilAuthenticator token(String clientToken, String accessToken) throws AuthenticationException {
        return token(clientToken, accessToken, YggdrasilAuthenticationServiceBuilder.buildDefault());
    }

    /**
     * Creates a <code>YggdrasilAuthenticator</code> with a customized
     * {@link AuthenticationService} and initializes it with a token.
     *
     * @param clientToken the client token
     * @param accessToken the access token
     * @param service     the customized {@link AuthenticationService}
     * @return a YggdrasilAuthenticator
     * @throws AuthenticationException If an exception occurs during the
     *                                 authentication
     */
    public static YggdrasilAuthenticator token(String clientToken, String accessToken, AuthenticationService service) throws AuthenticationException {
        Objects.requireNonNull(clientToken);
        Objects.requireNonNull(accessToken);
        Objects.requireNonNull(service);
        YggdrasilAuthenticator auth = new YggdrasilAuthenticator(service);
        auth.refreshWithToken(clientToken, accessToken);
        return auth;
    }

    /**
     * Creates a <code>YggdrasilAuthenticator</code>, and initializes it with
     * password.
     *
     * @param username the username
     * @param password the password
     * @return a YggdrasilAuthenticator
     * @throws AuthenticationException If an exception occurs during the
     *                                 authentication
     */
    public static YggdrasilAuthenticator password(String username, String password) throws AuthenticationException {
        return password(username, password, null);
    }

    /**
     * Creates a <code>YggdrasilAuthenticator</code>, and initializes it with
     * password.
     *
     * @param username          the username
     * @param password          the password
     * @param characterSelector the character selector
     * @return a YggdrasilAuthenticator
     * @throws AuthenticationException If an exception occurs during the
     *                                 authentication
     */
    public static YggdrasilAuthenticator password(String username, String password, CharacterSelector characterSelector) throws AuthenticationException {
        return password(username, password, characterSelector, UUIDUtils.randomUnsignedUUID());
    }

    /**
     * Creates a <code>YggdrasilAuthenticator</code> with the given client
     * token, and initializes it with password.
     *
     * @param username          the username
     * @param password          the password
     * @param characterSelector the character selector
     * @param clientToken       the client token
     * @return a YggdrasilAuthenticator
     * @throws AuthenticationException If an exception occurs during the
     *                                 authentication
     */
    public static YggdrasilAuthenticator password(String username, String password, CharacterSelector characterSelector, String clientToken) throws AuthenticationException {
        return password(username, password, characterSelector, clientToken, YggdrasilAuthenticationServiceBuilder.buildDefault());
    }

    /**
     * Creates a <code>YggdrasilAuthenticator</code> with a customized
     * {@link AuthenticationService} and the given client token, and initializes
     * it with password.
     *
     * @param username          the username
     * @param password          the password
     * @param characterSelector the character selector
     * @param clientToken       the client token
     * @param service           the customized {@link AuthenticationService}
     * @return a YggdrasilAuthenticator
     * @throws AuthenticationException If an exception occurs during the
     *                                 authentication
     */
    public static YggdrasilAuthenticator password(final String username, final String password, final CharacterSelector characterSelector, String clientToken, AuthenticationService service) throws AuthenticationException {
        return password(service, createPasswordProvider(username, password, characterSelector), clientToken);
    }

    /**
     * Creates a <code>YggdrasilAuthenticator</code> with a customized
     * {@link AuthenticationService} and the given client token, and initializes
     * it with password.
     *
     * @param service          the customized {@link AuthenticationService}
     * @param passwordProvider the password provider
     * @param clientToken      the client token
     * @return a YggdrasilAuthenticator
     * @throws AuthenticationException If an exception occurs during the
     *                                 authentication
     */
    public static YggdrasilAuthenticator password(AuthenticationService service, PasswordProvider passwordProvider, String clientToken) throws AuthenticationException {
        Objects.requireNonNull(service);
        Objects.requireNonNull(passwordProvider);
        Objects.requireNonNull(clientToken);
        YggdrasilAuthenticator auth = new YggdrasilAuthenticator(service);
        auth.refreshWithPassword(passwordProvider);
        return auth;
    }

    /**
     * Tries to get an available session, and export it as a {@link AuthInfo}.
     * If no profile is available, an {@link AuthenticationException} will be
     * thrown.
     * <p>
     * 尝试获得一个有效的 session ，并以 {@link AuthInfo} 的形式返回。 假如没有角色可以选择，则会抛出一个
     * {@link AuthenticationException} 。
     * <p>
     * {@inheritDoc}
     *
     * @see #session()
     */
    @Override
    public synchronized AuthInfo auth() throws AuthenticationException {
        GameProfile selectedProfile = session().getSelectedProfile();
        if (selectedProfile == null) {
            throw new AuthenticationException("no profile is available");
        }

        Map<String, String> properties = authResult.getProperties();
        if (properties == null) {
            properties = Collections.emptyMap();
        }

        return new AuthInfo(selectedProfile.getName(), authResult.getAccessToken(), selectedProfile.getUUID(), properties, authResult.getUserType().getName());
    }

    /**
     * Tries to get an available session.
     * <p>
     * The method will validate the current token first. If the current token is
     * not available, {@code YggdrasilAuthenticator} will try refreshing the
     * session. (see {@link #refresh()}).
     * <p>
     * 这个方法首先检查当前的 session 是否有效，假若无效则会试着刷新 session （见 {@link #refresh()} ）。
     *
     * @return an available session
     * @throws AuthenticationException if {@code YggdrasilAuthenticator}
     *                                 couldn't get an available session
     * @see #refresh()
     */
    @Override
    public synchronized Session session() throws AuthenticationException {
        if (authResult == null || !authenticationService.validate(authResult.getClientToken(), authResult.getAccessToken())) {
            refresh();
        }
        if (authResult == null) {
            throw new AuthenticationException("no authentication is available");
        }
        return authResult;
    }

    /**
     * Tries refreshing the current session.
     * <p>
     * This method will try refreshing the token. If YggdrasilAuthenticator
     * failed to refresh, it will call {@link #tryPasswordLogin()} to ask the
     * password for authentication. If no password is available,an
     * {@link AuthenticationException} will be thrown.
     * <p>
     * 尝试刷新当前的 session 。这个方法首先尝试使用 token 来刷新 session 。如果失败了， 则调用
     * {@link #tryPasswordLogin()} 来要求提供密码，使用密码进行登录。如果又失败了，则抛出一个
     * {@link AuthenticationException} 。
     *
     * @throws AuthenticationException if <code>YggdrasilAuthenticator</code>
     *                                 couldn't refresh the current session
     */
    public synchronized void refresh() throws AuthenticationException {
        if (authResult == null) {
            // refresh operation is not available
            PasswordProvider passwordProvider = tryPasswordLogin();
            if (passwordProvider == null) {
                throw new AuthenticationException("no more authentication methods to try");
            } else {
                refreshWithPassword(passwordProvider);
            }
        } else {
            try {
                refreshWithToken(authResult.getClientToken(), authResult.getAccessToken());
            } catch (AuthenticationException e) {
                // token login failed
                PasswordProvider passwordProvider = tryPasswordLogin();
                if (passwordProvider == null) {
                    throw e;
                }

                try {
                    refreshWithPassword(passwordProvider);
                } catch (AuthenticationException e1) {
                    e1.addSuppressed(e);
                    throw e1;
                }
            }
        }
    }

    /**
     * Refreshes the current session manually using password.
     *
     * @param username the username
     * @param password the password
     * @throws AuthenticationException If an exception occurs during the
     *                                 authentication
     */
    public synchronized void refreshWithPassword(String username, String password) throws AuthenticationException {
        refreshWithPassword(username, password, null);
    }

    /**
     * Refreshes the current session manually using password.
     *
     * @param username          the username
     * @param password          the password
     * @param characterSelector the character selector
     * @throws AuthenticationException If an exception occurs during the
     *                                 authentication
     */
    public synchronized void refreshWithPassword(String username, String password, CharacterSelector characterSelector) throws AuthenticationException {
        refreshWithPassword(createPasswordProvider(username, password, characterSelector));
    }

    /**
     * Refreshes the current session manually using password.
     *
     * @param passwordProvider the password provider
     * @throws AuthenticationException If an exception occurs during the
     *                                 authentication
     */
    public synchronized void refreshWithPassword(PasswordProvider passwordProvider) throws AuthenticationException {
        refreshWithPassword(passwordProvider, UUIDUtils.randomUnsignedUUID());
    }

    /**
     * Refreshes the current session manually using password.
     *
     * @param passwordProvider the password provider
     * @param clientToken      the client token
     * @throws AuthenticationException If an exception occurs during the
     *                                 authentication
     */
    public synchronized void refreshWithPassword(PasswordProvider passwordProvider, String clientToken) throws AuthenticationException {
        Objects.requireNonNull(passwordProvider);
        Objects.requireNonNull(clientToken);

        String username = passwordProvider.getUsername();
        String password = passwordProvider.getPassword();
        authResult = authenticationService.login(username, password, clientToken);
        if (authResult.getSelectedProfile() == null) {
            GameProfile[] profiles = authResult.getProfiles();
            if (profiles == null || profiles.length == 0) {
                return;
            }

            // no profile is selected
            // let's select one
            CharacterSelector selector = passwordProvider.getCharacterSelector();
            if (selector == null) {
                selector = new DefaultCharacterSelector();
            }

            GameProfile selectedProfile = selector.select(profiles);
            if (selectedProfile != null) {
                authResult = authenticationService.selectProfile(authResult.getClientToken(), authResult.getAccessToken(), selectedProfile);
            }
        }
    }

    /**
     * Refreshes the current session manually using token.
     *
     * @param clientToken the client token
     * @param accessToken the access token
     * @throws AuthenticationException If an exception occurs during the
     *                                 authentication
     */
    public synchronized void refreshWithToken(String clientToken, String accessToken) throws AuthenticationException {
        authResult = authenticationService.refresh(Objects.requireNonNull(clientToken), Objects.requireNonNull(accessToken));
    }

    /**
     * Clears the current session.
     */
    public synchronized void clearToken() {
        authResult = null;
    }

    /**
     * Gets the current session.
     *
     * @return the current session, {@code null} if the current session is
     * unavailable
     */
    public synchronized Session getCurrentSession() {
        return authResult;
    }

    /**
     * Sets the current session.
     *
     * @param session the session to set
     */
    public synchronized void setCurrentSession(Session session) {
        this.authResult = session;
    }

    /**
     * Gets the {@code YggdrasilAuthenticator}'s {@link AuthenticationService}.
     *
     * @return the {@code AuthenticationService}
     */
    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    /**
     * Provides the username and the password so that
     * {@code YggdrasilAuthenticator} can authenticate using password.
     * <p>
     * This method is usually called when the current token is invalid. If this
     * method returns {@code null}, the password authentication won't be
     * performed. The default implementation of the method returns {@code null}.
     * <p>
     * 当使用 token 登录失败时，就会调用此方法来要求提供用户名和密码，以便使用密码进行登录。 如果该方法返回 {@code null}
     * ，那么密码登录也将失败。该方法的默认实现返回 {@code null} 。
     *
     * @return the username and the password, can be null
     * @throws AuthenticationException If an exception occurs during the
     *                                 authentication
     */
    protected PasswordProvider tryPasswordLogin() throws AuthenticationException {
        return null;
    }

    /**
     * Creates an {@code AuthenticationService}.
     * <p>
     * This method is called during the deserialization to recreate an
     * {@code AuthenticationService}, because {@code YggdrasilAuthenticator}
     * doesn't persist {@code AuthenticationService} during the serialization.
     * The default implementation uses
     * {@link YggdrasilAuthenticationServiceBuilder#buildDefault()}.
     * <p>
     * {@code YggdrasilAuthenticator} 在序列化的时候不保存 {@code AuthenticationService} ，
     * 所以需要在反序列化的时候调用这个方法来重建一个 {@code AuthenticationService} 。 该方法的默认实现使用
     * {@link YggdrasilAuthenticationServiceBuilder#buildDefault()} 。
     *
     * @return an {@code AuthenticationService}
     */
    protected AuthenticationService createAuthenticationServiceForDeserialization() {
        return YggdrasilAuthenticationServiceBuilder.buildDefault();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        AuthenticationService newAuthenticationService = createAuthenticationServiceForDeserialization();

        if (newAuthenticationService == null) {
            throw new IllegalStateException("Cannot recreate AuthenticationService, createAuthenticationServiceForDeserialization() returns null.");
        }

        authenticationService = newAuthenticationService;
    }

    /**
     * Provides username and password for yggdrasil authentication.
     *
     * @author yushijinhun
     */
    public static interface PasswordProvider {

        /**
         * Gets the username to login.
         *
         * @return the username
         * @throws AuthenticationException If the username cannot be provided
         */
        String getUsername() throws AuthenticationException;

        /**
         * Gets the password of the user.
         *
         * @return the password
         * @throws AuthenticationException If the password cannot be provided
         */
        String getPassword() throws AuthenticationException;

        /**
         * Gets the character selector.
         * <p>
         * The method will be called when no character is selected. If this
         * method returns <code>null</code>, a {@link DefaultCharacterSelector}
         * will be used to select characters.
         *
         * @return a character selector, can be null
         */
        CharacterSelector getCharacterSelector();

    }

}
