package com.github.to2mbn.jmccc.auth;

import java.util.Objects;
import java.util.UUID;
import com.github.to2mbn.jyal.AuthenticationException;
import com.github.to2mbn.jyal.Session;

public class YggdrasilPasswordAuthenticator extends YggdrasilAuthenticator {

	private String email;
	private transient String password;

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
	public YggdrasilPasswordAuthenticator(String email, String password, CharacterSelector characterSelector) {
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
	public YggdrasilPasswordAuthenticator(String email, String password, CharacterSelector characterSelector, UUID clientToken) {
		super(clientToken, characterSelector);
		Objects.requireNonNull(email);
		Objects.requireNonNull(password);
		this.email = email;
		this.password = password;
	}

	@Override
	protected Session createSession() throws AuthenticationException {
		return getSessionService().login(email, password);
	}

}
