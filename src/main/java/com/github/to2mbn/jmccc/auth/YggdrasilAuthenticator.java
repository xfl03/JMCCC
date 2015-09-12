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

public class YggdrasilAuthenticator implements Authenticator {

	private String email;
	private String password;
	private YggdrasilCharacterSelector characterSelector;
	private YggdrasilSessionService sessionService = new YggdrasilSessionService(UUID.randomUUID().toString(), Agent.MINECRAFT);

	/**
	 * Creates a YggdrasilAuthenticator.
	 * 
	 * @param email the email
	 * @param password the password
	 * @throws NullPointerException if <code>email==null||password==null</code>
	 */
	public YggdrasilAuthenticator(String email, String password) {
		this(email, password, null);
	}

	/**
	 * Creates a YggdrasilAuthenticator.
	 * 
	 * @param email the email
	 * @param password the password
	 * @param characterSelector call when selecting character, null if use the default character
	 * @throws NullPointerException if <code>email==null||password==null</code>
	 */
	public YggdrasilAuthenticator(String email, String password, YggdrasilCharacterSelector characterSelector) {
		Objects.requireNonNull(email);
		Objects.requireNonNull(password);
		this.email = email;
		this.password = password;
		this.characterSelector = characterSelector;
	}

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
