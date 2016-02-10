package org.to2mbn.jmccc.auth.yggdrasil;

import java.util.Objects;
import java.util.UUID;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.yggdrasil.core.GameProfile;
import org.to2mbn.jmccc.auth.yggdrasil.core.Session;

/**
 * Yggdrasil authenticator using token.
 * <p>
 * This class is serializable. If you want to save the authentication (aka 'remember password'), you don't need to save
 * the password, just save this YggdrasilTokenAuthenticator object. It's safer because YggdrasilTokenAuthenticator only
 * saves the access token.
 * <p>
 * Use {@link #loginWithToken(String, String)}, {@link #loginWithToken(String, String, CharacterSelector)}, or
 * {@link #loginWithToken(String, String, CharacterSelector, UUID)} to create an instance.
 * 
 * @author yushijinhun
 */
public class YggdrasilTokenAuthenticator extends YggdrasilAuthenticator {

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
	 * {@link CharacterSelector#select(GameProfile, GameProfile[])} will be called during the authentication.
	 * 
	 * @param email the email of the account
	 * @param password the password
	 * @param characterSelector the character selector, null to select the default character
	 * @return the YggdrasilTokenAuthenticator
	 * @throws AuthenticationException if an exception has occurred during the authentication
	 * @throws NullPointerException if <code>email==null||password==null||clientToken==null</code>
	 */
	public static YggdrasilTokenAuthenticator loginWithToken(String email, String password, CharacterSelector characterSelector) throws AuthenticationException {
		return loginWithToken(email, password, characterSelector, UUID.randomUUID());
	}

	/**
	 * Login with password and create a YggdrasilTokenAuthenticator.
	 * <p>
	 * If <code>characterSelector!=null</code>, {@link CharacterSelector#select(GameProfile, GameProfile[])} will be
	 * called during the authentication.
	 * 
	 * @param email the email of the account
	 * @param password the password
	 * @param characterSelector the character selector, null to select the default character
	 * @param clientToken the client token
	 * @return the YggdrasilTokenAuthenticator
	 * @throws AuthenticationException if an exception has occurred during the authentication
	 * @throws NullPointerException if <code>email==null||password==null||clientToken==null</code>
	 */
	public static YggdrasilTokenAuthenticator loginWithToken(String email, String password, CharacterSelector characterSelector, UUID clientToken) throws AuthenticationException {
		// no need for null checks, YggdrasilPasswordAuthenticator.<init> does this
		return new YggdrasilTokenAuthenticator(clientToken, new YggdrasilPasswordAuthenticator(email, password, characterSelector, clientToken).auth().getToken());
	}

	private String accessToken;

	/**
	 * Creates a YggdrasilTokenAuthenticator with the given client token and the given access token.
	 * 
	 * @param clientToken the given client token
	 * @param accessToken the given access token
	 * @throws NullPointerException if <code>clientToken==null</code>
	 */
	public YggdrasilTokenAuthenticator(UUID clientToken, String accessToken) {
		super(clientToken);
		Objects.requireNonNull(accessToken);
		this.accessToken = accessToken;
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
			return getSessionService().isValid(accessToken);
	}

	@Override
	protected Session createSession() throws AuthenticationException {
		Session session = getSessionService().loginWithToken(accessToken);
		accessToken = session.getAccessToken();
		return session;
	}

}
