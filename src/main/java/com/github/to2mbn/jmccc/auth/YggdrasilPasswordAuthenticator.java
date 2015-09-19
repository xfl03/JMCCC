package com.github.to2mbn.jmccc.auth;

import java.util.Objects;
import java.util.UUID;
import org.json.JSONObject;
import com.github.to2mbn.jmccc.launch.AuthenticationException;
import com.github.to2mbn.jyal.Agent;
import com.github.to2mbn.jyal.GameProfile;
import com.github.to2mbn.jyal.Session;
import com.github.to2mbn.jyal.util.UUIDUtils;
import com.github.to2mbn.jyal.yggdrasil.YggdrasilSessionService;

public class YggdrasilPasswordAuthenticator implements Authenticator {

	private String email;
	private String password;
	private YggdrasilCharacterSelector characterSelector;
	private YggdrasilSessionService sessionService;

	/**
	 * Creates a YggdrasilPasswordAuthenticator.
	 * 
	 * @param email the email
	 * @param password the password
	 * @throws NullPointerException if <code>email==null||password==null</code>
	 */
	public YggdrasilPasswordAuthenticator(String email, String password) {
		this(email, password, null);
	}

	/**
	 * Creates a YggdrasilPasswordAuthenticator.
	 * 
	 * @param email the email
	 * @param password the password
	 * @param characterSelector call when selecting character, null if use the default character
	 * @throws NullPointerException if <code>email==null||password==null</code>
	 */
	public YggdrasilPasswordAuthenticator(String email, String password, YggdrasilCharacterSelector characterSelector) {
		this(email, password, null, UUID.randomUUID());
	}

	/**
	 * Creates a YggdrasilPasswordAuthenticator.
	 * 
	 * @param email the email
	 * @param password the password
	 * @param characterSelector call when selecting character, null if use the default character
	 * @param clientToken the client token
	 * @throws NullPointerException if <code>email==null||password==null||clientToken==null</code>
	 */
	public YggdrasilPasswordAuthenticator(String email, String password, YggdrasilCharacterSelector characterSelector, UUID clientToken) {
		Objects.requireNonNull(email);
		Objects.requireNonNull(password);
		Objects.requireNonNull(clientToken);
		this.email = email;
		this.password = password;
		this.characterSelector = characterSelector;
		sessionService = new YggdrasilSessionService(UUIDUtils.toUnsignedUUIDString(clientToken), Agent.MINECRAFT);
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
			session = sessionService.login(email, password);
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

		return new AuthResult(selected.getName(), session.getAccessToken(), UUIDUtils.toUnsignedUUIDString(selected.getUUID()), properties, session.getUserType().getName());
	}

}
