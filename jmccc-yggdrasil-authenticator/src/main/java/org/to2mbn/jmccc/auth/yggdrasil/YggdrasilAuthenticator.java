package org.to2mbn.jmccc.auth.yggdrasil;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.to2mbn.jmccc.auth.AuthInfo;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.Authenticator;
import org.to2mbn.jmccc.auth.yggdrasil.core.AuthenticationService;
import org.to2mbn.jmccc.auth.yggdrasil.core.GameProfile;
import org.to2mbn.jmccc.auth.yggdrasil.core.Session;
import org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil.YggdrasilServiceBuilder;
import org.to2mbn.jmccc.util.UUIDUtils;

/**
 * Provides yggdrasil authentication feature.
 * 
 * @author yushijinhun
 */
public class YggdrasilAuthenticator implements Authenticator, Serializable {

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
		 * The method will be invoked when no character is selected. If this
		 * method returns <code>null</code>, a {@link DefaultCharacterSelector}
		 * will be used to select characters.
		 * 
		 * @return a character selector, can be null
		 */
		CharacterSelector getCharacterSelector();

	}

	/**
	 * Creates a <code>YggdrasilAuthenticator</code> and initializes it with a
	 * token.
	 * 
	 * @param accessToken the access token
	 * @param clientToken the client token
	 * @return a YggdrasilAuthenticator
	 * @throws AuthenticationException If an exception occurs during the
	 *             authentication
	 */
	public static YggdrasilAuthenticator token(String accessToken, String clientToken) throws AuthenticationException {
		return token(accessToken, YggdrasilServiceBuilder.defaultAuthenticationService(clientToken));
	}

	/**
	 * Creates a <code>YggdrasilAuthenticator</code> with a customized
	 * {@link AuthenticationService} and initializes it with a token.
	 * 
	 * @param accessToken the access token
	 * @param service the customized {@link AuthenticationService}
	 * @return a YggdrasilAuthenticator
	 * @throws AuthenticationException If an exception occurs during the
	 *             authentication
	 */
	public static YggdrasilAuthenticator token(String accessToken, AuthenticationService service) throws AuthenticationException {
		YggdrasilAuthenticator auth = new YggdrasilAuthenticator(service);
		auth.refreshWithToken(accessToken);
		return auth;
	}

	/**
	 * Creates a <code>YggdrasilAuthenticator</code> and initializes it with
	 * password.
	 * 
	 * @param username the username
	 * @param password the password
	 * @return a YggdrasilAuthenticator
	 * @throws AuthenticationException If an exception occurs during the
	 *             authentication
	 */
	public static YggdrasilAuthenticator password(String username, String password) throws AuthenticationException {
		return password(username, password, null, YggdrasilServiceBuilder.defaultAuthenticationService());
	}

	/**
	 * Creates a <code>YggdrasilAuthenticator</code> and initializes it with
	 * password.
	 * 
	 * @param username the username
	 * @param password the password
	 * @param characterSelector the character selector
	 * @return a YggdrasilAuthenticator
	 * @throws AuthenticationException If an exception occurs during the
	 *             authentication
	 */
	public static YggdrasilAuthenticator password(String username, String password, CharacterSelector characterSelector) throws AuthenticationException {
		return password(username, password, characterSelector, YggdrasilServiceBuilder.defaultAuthenticationService());
	}

	/**
	 * Creates a <code>YggdrasilAuthenticator</code> with a specified client
	 * token and initializes it with password.
	 * 
	 * @param username the username
	 * @param password the password
	 * @param characterSelector the character selector
	 * @param clientToken the client token
	 * @return a YggdrasilAuthenticator
	 * @throws AuthenticationException If an exception occurs during the
	 *             authentication
	 */
	public static YggdrasilAuthenticator password(String username, String password, CharacterSelector characterSelector, String clientToken) throws AuthenticationException {
		return password(username, password, characterSelector, YggdrasilServiceBuilder.defaultAuthenticationService(clientToken));
	}

	/**
	 * Creates a <code>YggdrasilAuthenticator</code> with a customized
	 * {@link AuthenticationService} and initializes it with password.
	 * 
	 * @param username the username
	 * @param password the password
	 * @param characterSelector the character selector
	 * @param service the customized {@link AuthenticationService}
	 * @return a YggdrasilAuthenticator
	 * @throws AuthenticationException If an exception occurs during the
	 *             authentication
	 */
	public static YggdrasilAuthenticator password(final String username, final String password, final CharacterSelector characterSelector, AuthenticationService service) throws AuthenticationException {
		return password(service, new PasswordProvider() {

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
		});
	}

	/**
	 * Creates a <code>YggdrasilAuthenticator</code> with a customized
	 * {@link AuthenticationService} and initializes it with password.
	 * 
	 * @param service the customized {@link AuthenticationService}
	 * @param passwordProvider the password provider
	 * @return a YggdrasilAuthenticator
	 * @throws AuthenticationException If an exception occurs during the
	 *             authentication
	 */
	public static YggdrasilAuthenticator password(AuthenticationService service, PasswordProvider passwordProvider) throws AuthenticationException {
		YggdrasilAuthenticator auth = new YggdrasilAuthenticator(service);
		auth.refreshWithPassword(passwordProvider);
		return auth;
	}

	private static final long serialVersionUID = 1L;

	private AuthenticationService authenticationService;
	private volatile Session authResult;

	/**
	 * Constructs a YggdrasilAuthenticator with a random client token.
	 */
	public YggdrasilAuthenticator() {
		this(YggdrasilServiceBuilder.defaultAuthenticationService());
	}

	/**
	 * Constructs a YggdrasilAuthenticator with a specified client token.
	 * 
	 * @param clientToken the client token
	 */
	public YggdrasilAuthenticator(String clientToken) {
		this(YggdrasilServiceBuilder.defaultAuthenticationService(clientToken));
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

	/**
	 * Tries to get an available session, and export it as a {@link AuthInfo}.
	 * If no profile is available, an {@link AuthenticationException} will be
	 * thrown.
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

		return new AuthInfo(selectedProfile.getName(), authResult.getAccessToken(), UUIDUtils.unsign(selectedProfile.getUUID()), properties, authResult.getUserType().getName());
	}

	/**
	 * Tries to get an available session.
	 * <p>
	 * The method will validate the current token. If the current token is not
	 * available, <code>YggdrasilAuthenticator</code> will try refreshing the
	 * token. If YggdrasilAuthenticator failed to refresh, it will call
	 * {@link #tryPasswordLogin()} to ask the password for authentication. If no
	 * password is available, an <code>AuthenticationException</code> will be
	 * thrown.
	 * 
	 * @return an available session
	 * @throws AuthenticationException if <code>YggdrasilAuthenticator</code>
	 *             couldn't get an available session
	 */
	public synchronized Session session() throws AuthenticationException {
		if (authResult == null || !authenticationService.validate(authResult.getAccessToken())) {
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
	 * <code>AuthenticationException</code> will be thrown.
	 * 
	 * @throws AuthenticationException if <code>YggdrasilAuthenticator</code>
	 *             couldn't refresh the current session
	 */
	public synchronized void refresh() throws AuthenticationException {
		if (authResult == null) {
			// refresh operation is not available
			PasswordProvider passwordProvider = tryPasswordLogin();
			if (passwordProvider == null) {
				throw new AuthenticationException("no more authentication methods to try");
			}
			refreshWithPassword(passwordProvider);
		} else {
			try {
				refreshWithToken(authResult.getAccessToken());
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
	 * @param passwordProvider the password provider
	 * @throws AuthenticationException If an exception occurs during the
	 *             authentication
	 */
	public synchronized void refreshWithPassword(PasswordProvider passwordProvider) throws AuthenticationException {
		Objects.requireNonNull(passwordProvider);
		String username = passwordProvider.getUsername();
		String password = passwordProvider.getPassword();
		authResult = authenticationService.login(username, password);
		if (authResult.getSelectedProfile() == null) {
			// no profile is selected
			// let's select one
			CharacterSelector selector = passwordProvider.getCharacterSelector();
			if (selector == null) {
				selector = new DefaultCharacterSelector();
			}
			GameProfile[] profiles = authResult.getProfiles();
			if (profiles == null || profiles.length == 0) {
				throw new AuthenticationException("no profile is available");
			}
			GameProfile selectedProfile = selector.select(profiles);
			authResult = authenticationService.selectProfile(authResult.getAccessToken(), selectedProfile.getUUID());
		}
	}

	/**
	 * Refreshes the current session manually using token.
	 * 
	 * @param accessToken the access token
	 * @throws AuthenticationException If an exception occurs during the
	 *             authentication
	 */
	public synchronized void refreshWithToken(String accessToken) throws AuthenticationException {
		Objects.requireNonNull(accessToken);
		authResult = authenticationService.refresh(accessToken);
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
	 * @return the current session, <code>null</code> if the current session is
	 *         unavailable
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
	 * Gets the <code>YggdrasilAuthenticator</code>'s
	 * {@link AuthenticationService}.
	 * 
	 * @return the <code>AuthenticationService</code>
	 */
	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	/**
	 * Provides the username and the password so that
	 * <code>YggdrasilAuthenticator</code> can authenticate using password.
	 * <p>
	 * This method is usually invoked when the current token is invalid. If this
	 * method returns <code>null</code>, the password authentication won't be
	 * performed. The default implementation of the method returns
	 * <code>null</code>.
	 * 
	 * @return the username and the password, can be null
	 * @throws AuthenticationException If an exception occurs during the
	 *             authentication
	 */
	protected PasswordProvider tryPasswordLogin() throws AuthenticationException {
		return null;
	}

}
