package com.github.to2mbn.jmccc.auth;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;
import java.util.UUID;
import com.github.to2mbn.jyal.AuthenticationException;
import com.github.to2mbn.jyal.Session;

public class YggdrasilPasswordAuthenticator extends YggdrasilAuthenticator {

	private static final long serialVersionUID = 1L;

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

	/**
	 * Reads the encrypted password.
	 * <p>
	 * The default implementation throws a <code>NotSerializableException</code>. See
	 * {@link #writeEncryptedPassword(ObjectOutputStream, String)}
	 * 
	 * @param in the input
	 * @return the password
	 * @throws IOException if an I/O error has occurred
	 * @throws ClassNotFoundException if a ClassNotFoundException has occurred
	 * @see #writeEncryptedPassword(ObjectOutputStream, String)
	 */
	protected String readEncryptedPassword(ObjectInputStream in) throws IOException, ClassNotFoundException {
		throw new NotSerializableException();
	}

	/**
	 * Writes the encrypted password.
	 * <p>
	 * The default implementation throws a <code>NotSerializableException</code>, because saving plain password is too
	 * dangerous. If you need to save the password, you have to override this method. You should save the encrypted
	 * password instead of the plain password because of security.
	 * 
	 * @param out the output
	 * @param password the password
	 * @throws IOException if an I/O error has occurred
	 * @see #readEncryptedPassword(ObjectInputStream)
	 */
	protected void writeEncryptedPassword(ObjectOutputStream out, String password) throws IOException {
		throw new NotSerializableException();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		password = readEncryptedPassword(in);
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		writeEncryptedPassword(out, password);
	}

}
