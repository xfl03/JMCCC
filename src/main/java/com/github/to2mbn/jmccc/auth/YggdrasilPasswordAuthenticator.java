package com.github.to2mbn.jmccc.auth;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;
import java.util.UUID;
import com.github.to2mbn.jyal.AuthenticationException;
import com.github.to2mbn.jyal.GameProfile;
import com.github.to2mbn.jyal.Session;

/**
 * Yggdrasil authenticator using password.
 * <p>
 * This class is serializable, but we recommend you NOT to serialize this. If you want to remember the password, please
 * use {@link YggdrasilTokenAuthenticator} instead of this.
 * <p>
 * Notes for serialization:<br>
 * The character selector won't be serialized, you need to call {@link #setCharacterSelector(CharacterSelector)}
 * manually after deserialization.<br>
 * The default implementations of {@link #readEncryptedPassword(ObjectInputStream)} and
 * {@link #writeEncryptedPassword(ObjectOutputStream, String)} throws a <code>NotSerializableException</code> because of
 * security. You need to override these methods manually. See
 * {@link #writeEncryptedPassword(ObjectOutputStream, String)}.
 * 
 * @author yushijinhun
 */
public class YggdrasilPasswordAuthenticator extends YggdrasilAuthenticator {

	private static final long serialVersionUID = 1L;

	private String email;
	private transient String password;
	private transient CharacterSelector characterSelector;

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
		super(clientToken);
		Objects.requireNonNull(email);
		Objects.requireNonNull(password);
		this.email = email;
		this.password = password;
		this.characterSelector = characterSelector;
	}

	@Override
	protected Session createSession() throws AuthenticationException {
		return getSessionService().login(email, password);
	}

	@Override
	protected GameProfile selectCharacter(GameProfile selected, GameProfile[] availableProfiles) {
		if (characterSelector == null) {
			return super.selectCharacter(selected, availableProfiles);
		} else {
			return characterSelector.select(selected, availableProfiles);
		}
	}

	/**
	 * Gets the character selector, null for default character selector.
	 * <p>
	 * Notes: the character selector won't be serialized, you need to call
	 * {@link #setCharacterSelector(CharacterSelector)} manually after deserialization.
	 * 
	 * @return the character selector, null for default character selector
	 */
	public CharacterSelector getCharacterSelector() {
		return characterSelector;
	}

	/**
	 * Sets the character selector, null for default character selector.
	 * <p>
	 * Notes: the character selector won't be serialized, you need to call this method manually after deserialization.
	 * 
	 * @param characterSelector the character selector to set, null for default character selector
	 */
	public void setCharacterSelector(CharacterSelector characterSelector) {
		this.characterSelector = characterSelector;
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
	 * password instead of the plain password.
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
