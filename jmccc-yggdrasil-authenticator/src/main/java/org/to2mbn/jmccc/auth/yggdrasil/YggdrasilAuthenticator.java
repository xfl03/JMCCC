package org.to2mbn.jmccc.auth.yggdrasil;

import java.io.Serializable;
import java.util.Objects;
import org.json.JSONObject;
import org.to2mbn.jmccc.auth.AuthInfo;
import org.to2mbn.jmccc.auth.Authenticator;
import org.to2mbn.jmccc.auth.yggdrasil.core.Agent;
import org.to2mbn.jmccc.auth.yggdrasil.core.GameProfile;
import org.to2mbn.jmccc.auth.yggdrasil.core.Session;
import org.to2mbn.jmccc.auth.yggdrasil.core.SessionService;
import org.to2mbn.jmccc.auth.yggdrasil.core.util.UUIDUtils;
import org.to2mbn.jmccc.auth.yggdrasil.core.yggdrasil.YggdrasilSessionService;
import org.to2mbn.jmccc.auth.AuthenticationException;

abstract public class YggdrasilAuthenticator implements Authenticator, Serializable {

	private static final long serialVersionUID = 1L;

	private String clientToken;
	private transient SessionService sessionService;

	/**
	 * Creates a YggdrasilAuthenticator.
	 * 
	 * @param clientToken the client token
	 * @throws NullPointerException if <code>clientToken==null</code>
	 */
	public YggdrasilAuthenticator(String clientToken) {
		Objects.requireNonNull(clientToken);
		this.clientToken = clientToken;
	}

	/**
	 * Gets the client token.
	 * 
	 * @return the client token
	 */
	public String getClientToken() {
		return clientToken;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * {@link #selectCharacter(GameProfile, GameProfile[])} will be called during the authentication.
	 */
	@Override
	public AuthInfo auth() throws AuthenticationException {
		Session session;
		session = createSession();

		GameProfile selected = selectCharacter(session.getSelectedProfile(), session.getProfiles());
		if (selected == null) {
			throw new AuthenticationException("no character selected");
		}

		String properties;
		if (session.getProperties() == null) {
			properties = "{}";
		} else {
			properties = new JSONObject(session.getProperties()).toString();
		}

		return new AuthInfo(selected.getName(), session.getAccessToken(), UUIDUtils.unsign(selected.getUUID()), properties, session.getUserType().getName());
	}

	/**
	 * Creates a session for authentication.
	 * 
	 * @return the session
	 * @throws AuthenticationException if an authentication error has occurred
	 */
	abstract protected Session createSession() throws AuthenticationException;

	/**
	 * Gets the session service.
	 * <p>
	 * This is a lazy method. The session service will be created when the first time the method has been called.
	 * 
	 * @return the session service
	 */
	protected SessionService getSessionService() {
		if (sessionService == null) {
			sessionService = new YggdrasilSessionService(clientToken, Agent.MINECRAFT);
		}
		return sessionService;
	}

	/**
	 * Selects one of the given characters to login.
	 * <p>
	 * This method will be called during authentication. The default implementation returns <code>selected</code>. An
	 * {@link AuthenticationException} will occur if this method returns <code>null</code>.
	 * 
	 * @param selected the default character
	 * @param availableProfiles the available characters
	 * @return the character to login
	 */
	protected GameProfile selectCharacter(GameProfile selected, GameProfile[] availableProfiles) {
		return selected;
	}

}
