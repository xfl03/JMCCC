package com.github.to2mbn.jmccc.auth;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import org.json.JSONObject;
import com.github.to2mbn.jmccc.launch.AuthenticationException;
import com.github.to2mbn.jyal.Agent;
import com.github.to2mbn.jyal.GameProfile;
import com.github.to2mbn.jyal.Session;
import com.github.to2mbn.jyal.SessionService;
import com.github.to2mbn.jyal.util.UUIDUtils;
import com.github.to2mbn.jyal.yggdrasil.YggdrasilSessionService;

/**
 * Yggdrasil authenticator using token.
 * <p>
 * This class is serializable. If you want to save the authentication (aka 'remember password'), you don't need to save
 * the password, just save this YggdrasilTokenAuthenticator object. It's safer because YggdrasilTokenAuthenticator only
 * saves the access token.
 * <p>
 * Use {@link YggdrasilTokenAuthenticator#loginWithToken(String, String)},
 * {@link YggdrasilTokenAuthenticator#loginWithToken(String, String, YggdrasilCharacterSelector)}, or
 * {@link YggdrasilTokenAuthenticator#loginWithToken(String, String, YggdrasilCharacterSelector, UUID)} to create an
 * instance.
 * 
 * @author yushijinhun
 */
public class YggdrasilTokenAuthenticator implements Authenticator, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Login with password and create a YggdrasilTokenAuthenticator.
	 * <p>
	 * This method uses a randomized client token, and selects the default character.
	 * 
	 * @param email the email of the account
	 * @param password the password
	 * @return the YggdrasilTokenAuthenticator
	 * @throws AuthenticationException if an exception has occurred during the authentication
	 * @throws NullPointerException if <code>email==null||password==null||clientToken==null</code>
	 */
	public static YggdrasilTokenAuthenticator loginWithToken(String email, String password) throws AuthenticationException {
		return loginWithToken(email, password, null);
	}

	/**
	 * Login with password and create a YggdrasilTokenAuthenticator.
	 * <p>
	 * This method uses a randomized client token. If <code>characterSelector!=null</code>,
	 * {@link YggdrasilCharacterSelector#select(GameProfile, GameProfile[])} will be called during the authentication.
	 * 
	 * @param email the email of the account
	 * @param password the password
	 * @param characterSelector the character selector, null to select the default character
	 * @return the YggdrasilTokenAuthenticator
	 * @throws AuthenticationException if an exception has occurred during the authentication
	 * @throws NullPointerException if <code>email==null||password==null||clientToken==null</code>
	 */
	public static YggdrasilTokenAuthenticator loginWithToken(String email, String password, YggdrasilCharacterSelector characterSelector) throws AuthenticationException {
		return loginWithToken(email, password, characterSelector, UUID.randomUUID());
	}

	/**
	 * Login with password and create a YggdrasilTokenAuthenticator.
	 * <p>
	 * If <code>characterSelector!=null</code>, {@link YggdrasilCharacterSelector#select(GameProfile, GameProfile[])}
	 * will be called during the authentication.
	 * 
	 * @param email the email of the account
	 * @param password the password
	 * @param characterSelector the character selector, null to select the default character
	 * @param clientToken the client token
	 * @return the YggdrasilTokenAuthenticator
	 * @throws AuthenticationException if an exception has occurred during the authentication
	 * @throws NullPointerException if <code>email==null||password==null||clientToken==null</code>
	 */
	public static YggdrasilTokenAuthenticator loginWithToken(String email, String password, YggdrasilCharacterSelector characterSelector, UUID clientToken) throws AuthenticationException {
		// no need for null checks, YggdrasilPasswordAuthenticator.<init> does this
		return new YggdrasilTokenAuthenticator(clientToken, new YggdrasilPasswordAuthenticator(email, password, characterSelector, clientToken).auth().getToken());
	}

	private transient SessionService sessionService;

	private UUID clientToken;
	private String accessToken;

	/**
	 * Creates a YggdrasilTokenAuthenticator with the given client token and the given access token.
	 * 
	 * @param clientToken the given client token
	 * @param accessToken the given access token
	 */
	public YggdrasilTokenAuthenticator(UUID clientToken, String accessToken) {
		Objects.requireNonNull(clientToken);
		Objects.requireNonNull(accessToken);
		this.clientToken = clientToken;
		this.accessToken = accessToken;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method will update the stored access token after authentication.
	 */
	@Override
	public AuthResult auth() throws AuthenticationException {
		checkAndCreateSessionService();
		Session session;
		try {
			session = sessionService.loginWithToken(accessToken);
		} catch (com.github.to2mbn.jyal.AuthenticationException e) {
			throw new AuthenticationException(e);
		}

		accessToken = session.getAccessToken();

		GameProfile profile = session.getSelectedGameProfile();

		String properties;
		if (session.getUserProperties() == null) {
			properties = "{}";
		} else {
			properties = new JSONObject(session.getUserProperties()).toString();
		}

		return new AuthResult(profile.getName(), session.getAccessToken(), UUIDUtils.toUnsignedUUIDString(profile.getUUID()), properties, session.getUserType().getName());
	}

	/**
	 * Checks if the access token is valid.
	 * <p>
	 * If this method returns false, you shouldn't use this YggdrasilTokenAuthenticator any longer. You need to create
	 * another YggdrasilTokenAuthenticator by password authentication.
	 * 
	 * @return true if the access token is valid
	 * @throws AuthenticationException if an error has occurred during validating
	 */
	public boolean isValid() throws AuthenticationException {
		checkAndCreateSessionService();
		try {
			return sessionService.isValid(accessToken);
		} catch (com.github.to2mbn.jyal.AuthenticationException e) {
			throw new AuthenticationException("failed to valid access token", e);
		}
	}

	/**
	 * Gets the client token.
	 * <p>
	 * You should use the same client token as previous authentication if you want to auth with access token. The client
	 * token should be generated randomly, you shouldn't always use the same client token.
	 * 
	 * @return the client token
	 */
	public UUID getClientToken() {
		return clientToken;
	}

	private void checkAndCreateSessionService() {
		if (sessionService == null) {
			sessionService = new YggdrasilSessionService(UUIDUtils.toUnsignedUUIDString(clientToken), Agent.MINECRAFT);
		}
	}

}
