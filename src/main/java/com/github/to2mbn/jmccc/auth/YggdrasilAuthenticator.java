package com.github.to2mbn.jmccc.auth;

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

abstract public class YggdrasilAuthenticator implements Authenticator {

	private UUID clientToken;
	private transient SessionService sessionService;
	private transient YggdrasilCharacterSelector characterSelector;

	/**
	 * Creates a YggdrasilAuthenticator with default character.
	 * 
	 * @param clientToken the client token
	 * @throws NullPointerException if <code>clientToken==null</code>
	 */
	public YggdrasilAuthenticator(UUID clientToken) {
		this(clientToken, null);
	}

	/**
	 * Creates a YggdrasilAuthenticator.
	 * 
	 * @param clientToken the client token
	 * @param characterSelector call when selecting character, null if use the default character
	 * @throws NullPointerException if <code>clientToken==null</code>
	 */
	public YggdrasilAuthenticator(UUID clientToken, YggdrasilCharacterSelector characterSelector) {
		Objects.requireNonNull(clientToken);
		this.clientToken = clientToken;
		this.characterSelector = characterSelector;
	}

	/**
	 * Gets the client token.
	 * 
	 * @return the client token
	 */
	public UUID getClientToken() {
		return clientToken;
	}

	/**
	 * Gets the character selector, null if no character selector.
	 * 
	 * @return the character selector, null if no character selector
	 */
	public YggdrasilCharacterSelector getCharacterSelector() {
		return characterSelector;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * If <code>characterSelector!=null</code>, {@link YggdrasilCharacterSelector#select(GameProfile, GameProfile[])}
	 * will be called during authentication.
	 */
	@Override
	public AuthResult auth() throws AuthenticationException {
		Session session;
		try {
			session = createSession();
		} catch (com.github.to2mbn.jyal.AuthenticationException e) {
			throw new AuthenticationException(e);
		}

		GameProfile selected;
		if (characterSelector == null) {
			// select the default character
			selected = session.getSelectedGameProfile();
		} else {
			selected = characterSelector.select(session.getSelectedGameProfile(), session.getGameProfiles());
		}
		if (selected == null) {
			throw new AuthenticationException("no character selected");
		}

		String properties;
		if (session.getUserProperties() == null) {
			properties = "{}";
		} else {
			properties = new JSONObject(session.getUserProperties()).toString();
		}

		return new AuthResult(selected.getName(), session.getAccessToken(), UUIDUtils.unsign(selected.getUUID()), properties, session.getUserType().getName());
	}

	abstract protected Session createSession() throws com.github.to2mbn.jyal.AuthenticationException;

	protected SessionService getSessionService() {
		if (sessionService == null) {
			sessionService = new YggdrasilSessionService(clientToken, Agent.MINECRAFT);
		}
		return sessionService;
	}

}
